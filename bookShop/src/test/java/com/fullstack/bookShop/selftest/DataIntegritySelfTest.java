package com.fullstack.bookShop.selftest;

import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.model.Order;
import com.fullstack.bookShop.model.OrderDetail;
import com.fullstack.bookShop.repository.*;
import com.fullstack.bookShop.util.enums.StatusEnum;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Kiem tra toan ven du lieu va pham vi truy cap cua cac tinh nang dang co.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DataIntegritySelfTest {

    @Autowired
    private MockMvc mockMvc;

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

    private Book book;

    private Order order;

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

        book = new Book();
        book.setName("So Do");
        book.setAuthor("Vu Trong Phung");
        book.setPrice(120000.0);
        book.setQuantity(10);
        book.setIntroduceText("Gioi thieu");
        book.setDescription("Mo ta");
        book.setReader("Doc gia");
        book.setCategory(category);
        book = bookRepository.save(book);

        order = new Order();
        order.setName("Nguyen Van A");
        order.setEmail("buyer@bookshop.local");
        order.setPhone("0912345678");
        order.setAddress("So 1 Dai Co Viet, Hai Ba Trung, Ha Noi");
        order.setPaymentMethod("COD");
        order.setTotalPrice(240000.0);
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
    @DisplayName("BUG-H: xoa sach xoa luon dong hang trong don da hoan tat -> mat lich su ban hang")
    @WithMockUser(username = "admin@bookshop.local", roles = {"ADMIN"})
    void deleteBookMustNotDestroyCompletedOrderHistory() throws Exception {
        long detailsBefore = orderDetailRepository.count();

        mockMvc.perform(delete("/api/v1/books/{id}", book.getId())).andReturn();

        long detailsAfter = orderDetailRepository.count();
        boolean orderStillExists = orderRepository.existsById(order.getId());

        assertThat(orderStillExists)
                .as("don hang van phai ton tai")
                .isTrue();
        assertThat(detailsAfter)
                .as("dong hang cua don da hoan tat khong duoc bien mat (truoc=%d, sau=%d)",
                        detailsBefore, detailsAfter)
                .isEqualTo(detailsBefore);
    }

    @Test
    @DisplayName("BUG-I: /orders/history chi duoc tra don cua chinh nguoi dang dang nhap")
    @WithMockUser(username = "someone.else@bookshop.local", roles = {"USER"})
    void orderHistoryMustBeScopedToCurrentUser() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/orders/history")
                        .param("page", "1")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body)
                .as("nguoi dung khac khong duoc thay don cua buyer@bookshop.local, body=%s", body)
                .doesNotContain("buyer@bookshop.local");
    }

    @Test
    @DisplayName("BUG-J: user thuong khong duoc goi endpoint quan tri")
    @WithMockUser(username = "normal.user@bookshop.local", roles = {"USER"})
    void adminEndpointsMustRejectNormalUser() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/users")
                        .param("page", "1")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .as("user thuong goi /users phai bi tu choi 401/403, nhan duoc %d",
                        result.getResponse().getStatus())
                .isIn(401, 403);
    }
}
