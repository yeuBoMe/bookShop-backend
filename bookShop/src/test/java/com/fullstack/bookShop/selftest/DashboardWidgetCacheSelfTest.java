package com.fullstack.bookShop.selftest;

import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.model.Order;
import com.fullstack.bookShop.model.OrderDetail;
import com.fullstack.bookShop.repository.*;
import com.fullstack.bookShop.util.cache.TtlCache;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Endpoint theo tung widget (hoc tu dashboard FaceX) va cache TTL di kem.
 * Bat cache rieng cho lop test nay de do duoc tac dung that cua no.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "bookshop.cache.enabled=true")
@WithMockUser(username = "admin@bookshop.local", roles = {"ADMIN"})
class DashboardWidgetCacheSelfTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TtlCache ttlCache;

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
        ttlCache.clear();

        orderDetailRepository.deleteAll();
        orderRepository.deleteAll();
        ratingRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = new Category();
        category.setName("Van hoc");
        category = categoryRepository.save(category);

        Book book = new Book();
        book.setName("So Do");
        book.setAuthor("Vu Trong Phung");
        book.setPrice(100_000.0);
        book.setQuantity(3);
        book.setIntroduceText("Gioi thieu");
        book.setDescription("Mo ta");
        book.setReader("Doc gia");
        book.setCategory(category);
        book = bookRepository.save(book);

        Order order = new Order();
        order.setName("Khach Mot");
        order.setEmail("khach1@bookshop.local");
        order.setPhone("0912345678");
        order.setAddress("So 1 Dai Co Viet, Hai Ba Trung, Ha Noi");
        order.setPaymentMethod("COD");
        order.setTotalPrice(200_000.0);
        order.setStatus(StatusEnum.SUCCESS);
        order = orderRepository.save(order);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(order);
        orderDetail.setBook(book);
        orderDetail.setBookName(book.getName());
        orderDetail.setQuantity(2);
        orderDetailRepository.save(orderDetail);
    }

    @Test
    @DisplayName("WIDGET: moi widget goi duoc doc lap, khong phai keo ca man hinh")
    void everyWidgetMustBeCallableOnItsOwn() throws Exception {
        List<String> widgets = List.of(
                "/api/v1/dashboard/revenue",
                "/api/v1/dashboard/revenue-by-day",
                "/api/v1/dashboard/order-status",
                "/api/v1/dashboard/top-books",
                "/api/v1/dashboard/top-customers",
                "/api/v1/dashboard/low-stock");

        for (String widget : widgets) {
            int status = mockMvc.perform(get(widget).accept(MediaType.APPLICATION_JSON))
                    .andReturn()
                    .getResponse()
                    .getStatus();

            assertThat(status).as("%s phai tra 200", widget).isEqualTo(200);
        }
    }

    @Test
    @DisplayName("CACHE: goi lai cung widget khong ban them cau SQL nao")
    void secondCallMustBeServedFromCache() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/top-books").accept(MediaType.APPLICATION_JSON)).andReturn();

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        mockMvc.perform(get("/api/v1/dashboard/top-books").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data[0].name").value("So Do"));

        assertThat(statistics.getPrepareStatementCount())
                .as("lan goi thu hai phai lay tu cache, khong cham DB")
                .isZero();
    }

    @Test
    @DisplayName("CACHE: xoa cache thi so lieu tuoi lai ngay")
    void clearingCacheMustReturnFreshNumbers() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/low-stock").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data[0].quantity").value(3));

        Book book = bookRepository.findAll().getFirst();
        book.setQuantity(1);
        bookRepository.save(book);

        // Con trong han TTL nen van tra so cu - dung la dieu mong doi cua cache
        mockMvc.perform(get("/api/v1/dashboard/low-stock").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data[0].quantity").value(3));

        ttlCache.clear();

        mockMvc.perform(get("/api/v1/dashboard/low-stock").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data[0].quantity").value(1));
    }

    @Test
    @DisplayName("CACHE: doi khoang thoi gian thi khong an nham ket qua cua khoang khac")
    void differentTimeRangeMustNotShareCacheEntry() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/revenue")
                        .param("from", "2020-01-01T00:00:00Z")
                        .param("to", "2020-12-31T23:59:59Z")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.totalRevenue").value(0.0));

        mockMvc.perform(get("/api/v1/dashboard/revenue").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.totalRevenue").value(200_000.0));
    }
}
