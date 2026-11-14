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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

/**
 * Da tat open-in-view: quan he LAZY khong con duoc nap am tham o tang view nua.
 * Nhung endpoint map entity NGOAI transaction phai duoc chung minh la van chay.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "admin@bookshop.local", roles = {"ADMIN"})
class LazyLoadingSelfTest {

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

        Book book = new Book();
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
        order.setStatus(StatusEnum.PENDING);
        order = orderRepository.save(order);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(order);
        orderDetail.setBook(book);
        orderDetail.setBookName(book.getName());
        orderDetail.setQuantity(2);
        orderDetailRepository.save(orderDetail);
    }

    @Test
    @DisplayName("LAZY: GET /orders/{id} phai tra ve day du dong hang khi da tat open-in-view")
    void getOrderByIdMustStillLoadDetails() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/orders/{id}", order.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(result.getResponse().getStatus())
                .as("body=%s", body)
                .isEqualTo(200);
        assertThat(body)
                .as("dong hang phai co trong response, body=%s", body)
                .contains("So Do");
    }

    @Test
    @DisplayName("LAZY: PATCH /orders/{id} phai map duoc response sau khi transaction dong")
    void patchOrderMustStillMapResponse() throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/v1/orders/{id}", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHIPPING\"}"))
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .as("body=%s", result.getResponse().getContentAsString())
                .isEqualTo(200);
    }

    @Test
    @DisplayName("LAZY: GET /orders/history phai tra ve dong hang cua tung don")
    @WithMockUser(username = "buyer@bookshop.local", roles = {"USER"})
    void orderHistoryMustLoadDetails() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/orders/history")
                        .param("page", "1")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(result.getResponse().getStatus())
                .as("body=%s", body)
                .isEqualTo(200);
        assertThat(body)
                .as("don cua chinh nguoi dung phai kem dong hang, body=%s", body)
                .contains("So Do");
    }
}
