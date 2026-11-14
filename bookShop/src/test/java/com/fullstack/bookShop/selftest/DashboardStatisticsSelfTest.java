package com.fullstack.bookShop.selftest;

import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.model.Order;
import com.fullstack.bookShop.model.OrderDetail;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * So lieu thong ke phai khop voi con so tu tinh tay tu du lieu seed,
 * khong chi kiem tra "endpoint tra 200".
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "admin@bookshop.local", roles = {"ADMIN"})
class DashboardStatisticsSelfTest {

    private static final String ENDPOINT = "/api/v1/dashboard/statistics";

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

    private Book cheapBook;

    private Book expensiveBook;

    @BeforeEach
    void seed() {
        orderDetailRepository.deleteAll();
        orderRepository.deleteAll();
        ratingRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = new Category();
        category.setName("Van hoc");
        category = categoryRepository.save(category);

        cheapBook = bookRepository.save(newBook("Sach Re", "Tac Gia A", 50_000.0, category));
        expensiveBook = bookRepository.save(newBook("Sach Dat", "Tac Gia B", 200_000.0, category));

        // Doanh thu chi tinh don SUCCESS + PAID
        saveOrder("khach1@bookshop.local", "Khach Mot", StatusEnum.SUCCESS, 300_000.0, cheapBook, 2);
        saveOrder("khach1@bookshop.local", "Khach Mot", StatusEnum.PAID, 200_000.0, expensiveBook, 1);
        saveOrder("khach2@bookshop.local", "Khach Hai", StatusEnum.SUCCESS, 100_000.0, cheapBook, 1);

        // Hai don duoi day KHONG duoc tinh vao doanh thu
        saveOrder("khach3@bookshop.local", "Khach Ba", StatusEnum.PENDING, 999_000.0, cheapBook, 5);
        saveOrder("khach4@bookshop.local", "Khach Bon", StatusEnum.CANCEL, 888_000.0, cheapBook, 5);
    }

    @Test
    @DisplayName("STATS: doanh thu chi cong don SUCCESS va PAID, bo qua PENDING va CANCEL")
    void revenueMustCountOnlyPaidOrders() throws Exception {
        mockMvc.perform(get(ENDPOINT).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.revenue.totalRevenue").value(600_000.0))
                .andExpect(jsonPath("$.data.revenue.paidOrderCount").value(3))
                .andExpect(jsonPath("$.data.revenue.averageOrderValue").value(200_000.0));
    }

    @Test
    @DisplayName("STATS: phan bo trang thai phai dem du ca don chua thanh toan")
    void statusBreakdownMustCountEveryOrder() throws Exception {
        MvcResult result = mockMvc.perform(get(ENDPOINT).accept(MediaType.APPLICATION_JSON)).andReturn();
        String body = result.getResponse().getContentAsString();

        assertThat(body).as("body=%s", body)
                .contains("\"SUCCESS\"")
                .contains("\"PENDING\"")
                .contains("\"CANCEL\"");
    }

    @Test
    @DisplayName("STATS: sach ban chay xep theo so luong ban, chi tinh don da thanh toan")
    void topSellingBooksMustRankBySoldQuantity() throws Exception {
        mockMvc.perform(get(ENDPOINT).accept(MediaType.APPLICATION_JSON))
                // Sach Re ban 3 cuon (2 + 1) qua don SUCCESS/PAID; don PENDING 5 cuon khong duoc tinh
                .andExpect(jsonPath("$.data.topSellingBooks[0].name").value("Sach Re"))
                .andExpect(jsonPath("$.data.topSellingBooks[0].soldQuantity").value(3))
                .andExpect(jsonPath("$.data.topSellingBooks[1].name").value("Sach Dat"))
                .andExpect(jsonPath("$.data.topSellingBooks[1].soldQuantity").value(1));
    }

    @Test
    @DisplayName("STATS: khach chi tieu nhieu nhat xep theo tong tien da thanh toan")
    void topCustomersMustRankByTotalSpent() throws Exception {
        mockMvc.perform(get(ENDPOINT).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.topCustomers[0].email").value("khach1@bookshop.local"))
                .andExpect(jsonPath("$.data.topCustomers[0].totalSpent").value(500_000.0))
                .andExpect(jsonPath("$.data.topCustomers[0].orderCount").value(2));
    }

    @Test
    @DisplayName("STATS: canh bao sach sap het hang theo nguong cau hinh")
    void lowStockBooksMustBeReported() throws Exception {
        Book almostOutOfStock = bookRepository.findById(cheapBook.getId()).orElseThrow();
        almostOutOfStock.setQuantity(2);
        bookRepository.save(almostOutOfStock);

        mockMvc.perform(get(ENDPOINT).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.lowStockBooks[0].name").value("Sach Re"))
                .andExpect(jsonPath("$.data.lowStockBooks[0].quantity").value(2));
    }

    @Test
    @DisplayName("STATS: so cau SQL co dinh, khong tang theo so don hang")
    void statisticsMustUseFixedNumberOfQueries() throws Exception {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        mockMvc.perform(get(ENDPOINT).accept(MediaType.APPLICATION_JSON)).andReturn();

        long executed = statistics.getPrepareStatementCount();
        assertThat(executed)
                .as("7 phep gom nhom -> toi da 7 cau SQL, thuc te %d", executed)
                .isLessThanOrEqualTo(7);
    }

    private Book newBook(String name, String author, double price, Category category) {
        Book book = new Book();
        book.setName(name);
        book.setAuthor(author);
        book.setPrice(price);
        book.setQuantity(100);
        book.setIntroduceText("Gioi thieu");
        book.setDescription("Mo ta");
        book.setReader("Doc gia");
        book.setCategory(category);
        return book;
    }

    private void saveOrder(String email, String name, StatusEnum status, double totalPrice, Book book, int quantity) {
        Order order = new Order();
        order.setName(name);
        order.setEmail(email);
        order.setPhone("0912345678");
        order.setAddress("So 1 Dai Co Viet, Hai Ba Trung, Ha Noi");
        order.setPaymentMethod("COD");
        order.setTotalPrice(totalPrice);
        order.setStatus(status);
        order = orderRepository.save(order);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(order);
        orderDetail.setBook(book);
        orderDetail.setBookName(book.getName());
        orderDetail.setQuantity(quantity);
        orderDetailRepository.save(orderDetail);
    }
}
