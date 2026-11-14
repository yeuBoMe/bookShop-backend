package com.fullstack.bookShop.selftest;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Do tai muc 5.000+ dong: dem so cau SQL va thoi gian tung endpoint.
 *
 * Luu y doc ket qua: chay tren H2 nen CON SO TUYET DOI khong the suy ra Postgres
 * (H2 khong co pg_trgm, khong co GIN, planner khac han). Cai lay duoc o day la
 * XU HUONG: query count co tang theo so dong khong, endpoint nao nap ca bang vao RAM.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "admin@bookshop.local", roles = {"ADMIN"})
class ScaleBenchmarkSelfTest {

    private static final int CATEGORY_COUNT = 20;

    private static final int BOOK_COUNT = 5_000;

    private static final int RATING_COUNT = 5_000;

    private static final int ORDER_COUNT = 2_000;

    private static final int DETAIL_PER_ORDER = 3;

    /**
     * Seed bang id tuong minh o dai cao, tach han khoi dai ma sequence cap phat.
     * Truoc day dung ALTER SEQUENCE ... RESTART de keo sequence len, nhung lam vay la giat
     * sequence ngay duoi chan optimizer dang giu san mot khoi id trong bo nho -> trung id.
     */
    private static final long ID_BASE = 1_000_000L;

    private static boolean seeded = false;

    private static final List<String> REPORT = new ArrayList<>();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @BeforeAll
    static void banner() {
        REPORT.clear();
    }

    @Test
    @DisplayName("BENCH: do tai 5.000 sach / 2.000 don / 5.000 danh gia")
    void benchmarkAtScale() throws Exception {
        seedOnce();

        report("--------------------------------------------------------------");
        report(String.format("DU LIEU: %d sach, %d don, %d dong hang, %d danh gia",
                count("books"), count("orders"), count("order_details"), count("ratings")));
        report("--------------------------------------------------------------");
        report(String.format("%-46s %8s %8s", "ENDPOINT", "SQL", "ms"));
        report("--------------------------------------------------------------");

        measure("GET /books                       trang 1", "/api/v1/books", "1", "20", null);
        measure("GET /books                       trang 250 (cuoi)", "/api/v1/books", "250", "20", null);
        measure("GET /books   tim kiem LIKE '%Sach 4%'", "/api/v1/books", "1", "20", "name~~'Sach 4'");
        measure("GET /books   loc category + sort gia", "/api/v1/books", "1", "20", "category.name:'The loai C'");
        measure("GET /books/average-rating        minRating=4", "/api/v1/books/average-rating", "1", "20", null);
        measure("GET /orders                      trang 1", "/api/v1/orders", "1", "20", null);
        measure("GET /orders                      trang 100 (cuoi)", "/api/v1/orders", "100", "20", null);
        measure("GET /ratings                     trang 1", "/api/v1/ratings", "1", "20", null);
        measure("GET /users                       trang 1", "/api/v1/users", "1", "20", null);
        measure("GET /categories                  trang 1", "/api/v1/categories", "1", "20", null);
        measure("GET /dashboard", "/api/v1/dashboard", null, null, null);

        report("--------------------------------------------------------------");

        REPORT.forEach(System.out::println);
    }

    private void measure(String label, String endpoint, String page, String size, String filter) throws Exception {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        var request = get(endpoint).accept(MediaType.APPLICATION_JSON);
        if (page != null) {
            request = request.param("page", page).param("size", size);
        }
        if (filter != null) {
            request = request.param("filter", filter);
        }
        if (endpoint.contains("average-rating")) {
            request = request.param("minRating", "4");
        }

        long startedAt = System.nanoTime();
        MvcResult result = mockMvc.perform(request).andReturn();
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;

        long queries = statistics.getPrepareStatementCount();
        int status = result.getResponse().getStatus();

        report(String.format("%-46s %8d %8d%s",
                label, queries, elapsedMs, status == 200 ? "" : "   <-- HTTP " + status));
    }

    private static void report(String line) {
        REPORT.add(line);
    }

    private long count(String table) {
        Long value = jdbcTemplate.queryForObject("SELECT count(*) FROM " + table, Long.class);
        return value != null ? value : 0L;
    }

    private void seedOnce() {
        if (seeded) {
            return;
        }

        jdbcTemplate.update("DELETE FROM order_details");
        jdbcTemplate.update("DELETE FROM orders");
        jdbcTemplate.update("DELETE FROM ratings");
        jdbcTemplate.update("DELETE FROM books");
        jdbcTemplate.update("DELETE FROM categories");

        List<Object[]> categories = new ArrayList<>();
        for (int i = 1; i <= CATEGORY_COUNT; i++) {
            categories.add(new Object[]{ID_BASE + i, "The loai " + (char) ('A' + (i - 1) % 26)});
        }
        jdbcTemplate.batchUpdate(
                "INSERT INTO categories (id, name, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)", categories);

        List<Object[]> books = new ArrayList<>();
        for (int i = 1; i <= BOOK_COUNT; i++) {
            books.add(new Object[]{
                    ID_BASE + i,
                    "Sach " + i,
                    "Tac gia " + (char) ('A' + i % 26),
                    50_000.0 + (i % 500) * 1_000,
                    50,
                    "Gioi thieu sach " + i,
                    "Mo ta chi tiet cua cuon sach so " + i,
                    "Doc gia pho thong",
                    ID_BASE + (i % CATEGORY_COUNT) + 1,
                    (double) ((i % 5) + 1),
                    1
            });
        }
        jdbcTemplate.batchUpdate("""
                INSERT INTO books
                    (id, name, author, price, quantity, introduce_text, description, reader,
                     category_id, average_stars, rating_count, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """, books);

        List<Long> existingUserIds = jdbcTemplate.queryForList(
                "SELECT id FROM users ORDER BY id LIMIT 1", Long.class);
        Long userId = existingUserIds.isEmpty() ? null : existingUserIds.get(0);
        if (userId == null) {
            jdbcTemplate.update("""
                    INSERT INTO users (id, email, full_name, password, created_at)
                    VALUES (900001, 'bench@bookshop.local', 'Bench Reader', 'hash-placeholder', CURRENT_TIMESTAMP)
                    """);
            userId = 900_001L;
        }

        List<Object[]> ratings = new ArrayList<>();
        for (int i = 1; i <= RATING_COUNT; i++) {
            ratings.add(new Object[]{ID_BASE + i, (i % 5) + 1, "Nhan xet so " + i, ID_BASE + i, userId});
        }
        jdbcTemplate.batchUpdate("""
                INSERT INTO ratings (id, stars, comment, book_id, user_id, created_at)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """, ratings);

        List<Object[]> orders = new ArrayList<>();
        for (int i = 1; i <= ORDER_COUNT; i++) {
            orders.add(new Object[]{
                    ID_BASE + i,
                    "Khach hang " + (char) ('A' + i % 26),
                    "khach" + i + "@bookshop.local",
                    "0912345678",
                    "So " + i + " Dai Co Viet, Hai Ba Trung, Ha Noi",
                    "COD",
                    250_000.0,
                    "SUCCESS"
            });
        }
        jdbcTemplate.batchUpdate("""
                INSERT INTO orders
                    (id, name, email, phone, address, payment_method, total_price, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """, orders);

        List<Object[]> details = new ArrayList<>();
        int detailId = 1;
        for (int orderId = 1; orderId <= ORDER_COUNT; orderId++) {
            for (int k = 0; k < DETAIL_PER_ORDER; k++) {
                int bookId = ((orderId * DETAIL_PER_ORDER + k) % BOOK_COUNT) + 1;
                details.add(new Object[]{ID_BASE + detailId++, 1, "Sach " + bookId, ID_BASE + bookId, ID_BASE + orderId});
            }
        }
        jdbcTemplate.batchUpdate(
                "INSERT INTO order_details (id, quantity, book_name, book_id, order_id) VALUES (?, ?, ?, ?, ?)",
                details);

        seeded = true;
    }
}
