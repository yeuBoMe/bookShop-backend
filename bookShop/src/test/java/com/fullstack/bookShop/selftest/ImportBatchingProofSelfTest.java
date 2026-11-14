package com.fullstack.bookShop.selftest;

import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.repository.*;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Do that xem import co gom lo o muc JDBC hay khong.
 *
 * <p>Khong assert - chi in so lieu de doi chieu hai kieu import. Ket luan nam o bao cao,
 * khong doan tu doc code.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "admin@bookshop.local", roles = {"ADMIN"})
class ImportBatchingProofSelfTest {

    private static final int ROW_COUNT = 120;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @BeforeEach
    void seed() {
        orderDetailRepository.deleteAll();
        orderRepository.deleteAll();
        ratingRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = new Category();
        category.setName("Van hoc");
        categoryRepository.save(category);
    }

    @Test
    @DisplayName("PROOF: so cau SQL va so transaction cua hai kieu import")
    void compareBatchingBetweenBothImportStyles() throws Exception {
        measure("import CU   /api/v1/books/import      ", "/api/v1/books/import", false);

        seed();

        measure("import MOI  /api/v1/books/import/batch", "/api/v1/books/import/batch", true);
    }

    private void measure(String label, String endpoint, boolean withAction) throws Exception {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        long startedAt = System.nanoTime();
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildPayload(withAction)))
                .andReturn();
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;

        System.out.printf(
                "%s | rows=%d | SQL=%d | insert=%d | transaction=%d | %d ms | luu duoc=%d%n",
                label,
                ROW_COUNT,
                statistics.getPrepareStatementCount(),
                statistics.getEntityInsertCount(),
                statistics.getTransactionCount(),
                elapsedMs,
                bookRepository.count());
    }

    private String buildPayload(boolean withAction) {
        return IntStream.rangeClosed(1, ROW_COUNT)
                .mapToObj(index -> "{"
                        + (withAction ? "\"action\":\"CREATE\"," : "")
                        + "\"name\":\"Sach So " + index + "\","
                        + "\"author\":\"Tac Gia " + (char) ('A' + index % 26) + "\","
                        + "\"introduceText\":\"Gioi thieu\","
                        + "\"description\":\"Mo ta chi tiet\","
                        + "\"reader\":\"Doc gia pho thong\","
                        + "\"price\":120000,"
                        + "\"quantity\":10,"
                        + "\"category\":\"Van hoc\""
                        + "}")
                .collect(Collectors.joining(",", "[", "]"));
    }
}
