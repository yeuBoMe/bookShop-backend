package com.fullstack.bookShop.selftest;

import com.fullstack.bookShop.model.*;
import com.fullstack.bookShop.repository.*;
import com.fullstack.bookShop.util.enums.StatusEnum;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Dem so cau SQL that su chay ra cho moi endpoint danh sach.
 * So query phai gan nhu khong doi khi so dong tang, neu tang tuyen tinh la dang dinh N+1.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "admin@bookshop.local", roles = {"ADMIN"})
class QueryCountSelfTest {

    private static final int PAGE_SIZE = 20;

    /** Nguong rong rai: 1 count + 1 select + vai query phu. N+1 se vuot xa muc nay. */
    private static final int MAX_QUERIES_PER_LIST_CALL = 8;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @BeforeEach
    void seed() {
        orderDetailRepository.deleteAll();
        orderRepository.deleteAll();
        ratingRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();

        for (int categoryIndex = 0; categoryIndex < 5; categoryIndex++) {
            Category category = new Category();
            category.setName("The loai " + categoryIndex);
            category = categoryRepository.save(category);

            for (int bookIndex = 0; bookIndex < 10; bookIndex++) {
                bookRepository.save(newBook(category, categoryIndex, bookIndex));
            }
        }

        User reader = userRepository.findByEmail("querycount.reader@bookshop.local")
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail("querycount.reader@bookshop.local");
                    user.setFullName("Query Count Reader");
                    user.setPassword("hash-placeholder");
                    return userRepository.save(user);
                });

        for (Book book : bookRepository.findAll()) {
            Rating rating = new Rating();
            rating.setBook(book);
            rating.setUser(reader);
            rating.setStars(4);
            rating.setComment("Nhan xet");
            ratingRepository.save(rating);
        }

        for (int orderIndex = 0; orderIndex < 25; orderIndex++) {
            Order order = new Order();
            order.setName("Khach hang " + (char) ('A' + orderIndex % 26));
            order.setEmail("khach" + orderIndex + "@bookshop.local");
            order.setPhone("091234567" + (orderIndex % 10));
            order.setAddress("So " + (orderIndex + 1) + " Dai Co Viet, Hai Ba Trung, Ha Noi");
            order.setPaymentMethod("COD");
            order.setTotalPrice(100000.0);
            order.setStatus(StatusEnum.SUCCESS);
            order = orderRepository.save(order);

            for (Book book : bookRepository.findAll().subList(0, 3)) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrder(order);
                orderDetail.setBook(book);
                orderDetail.setBookName(book.getName());
                orderDetail.setQuantity(1);
                orderDetailRepository.save(orderDetail);
            }
        }
    }

    @Test
    @DisplayName("PERF: GET /books khong duoc N+1 theo category")
    void bookListMustNotBeNPlusOne() throws Exception {
        assertQueryCountUnderLimit("/api/v1/books");
    }

    @Test
    @DisplayName("PERF: GET /orders khong duoc N+1 theo order detail va book")
    void orderListMustNotBeNPlusOne() throws Exception {
        assertQueryCountUnderLimit("/api/v1/orders");
    }

    @Test
    @DisplayName("PERF: GET /ratings khong duoc N+1 theo user va book")
    void ratingListMustNotBeNPlusOne() throws Exception {
        assertQueryCountUnderLimit("/api/v1/ratings");
    }

    @Test
    @DisplayName("PERF: GET /users khong duoc N+1 theo role")
    void userListMustNotBeNPlusOne() throws Exception {
        assertQueryCountUnderLimit("/api/v1/users");
    }

    private void assertQueryCountUnderLimit(String endpoint) throws Exception {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        mockMvc.perform(get(endpoint)
                        .param("page", "1")
                        .param("size", String.valueOf(PAGE_SIZE))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        long executed = statistics.getPrepareStatementCount();
        assertThat(executed)
                .as("%s voi page size %d chay %d cau SQL (nguong %d) - so query khong duoc tang theo so dong",
                        endpoint, PAGE_SIZE, executed, MAX_QUERIES_PER_LIST_CALL)
                .isLessThanOrEqualTo(MAX_QUERIES_PER_LIST_CALL);
    }

    private Book newBook(Category category, int categoryIndex, int bookIndex) {
        Book book = new Book();
        book.setName("Sach " + categoryIndex + "-" + bookIndex);
        book.setAuthor("Tac gia " + (char) ('A' + categoryIndex));
        book.setPrice(100000.0 + bookIndex);
        book.setQuantity(50);
        book.setIntroduceText("Gioi thieu");
        book.setDescription("Mo ta");
        book.setReader("Doc gia");
        book.setCategory(category);
        return book;
    }
}
