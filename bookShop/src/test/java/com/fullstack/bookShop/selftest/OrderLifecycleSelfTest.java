package com.fullstack.bookShop.selftest;

import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.model.Order;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Vong doi don hang: ke toan ton kho va luat chuyen trang thai.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "admin@bookshop.local", roles = {"ADMIN"})
class OrderLifecycleSelfTest {

    private static final int INITIAL_STOCK = 10;

    private static final double BOOK_PRICE = 120_000.0;

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

        Book newBook = new Book();
        newBook.setName("So Do");
        newBook.setAuthor("Vu Trong Phung");
        newBook.setPrice(BOOK_PRICE);
        newBook.setQuantity(INITIAL_STOCK);
        newBook.setIntroduceText("Gioi thieu");
        newBook.setDescription("Mo ta");
        newBook.setReader("Doc gia");
        newBook.setCategory(category);
        book = bookRepository.save(newBook);
    }

    @Test
    @DisplayName("STOCK: huy don phai hoan ton kho, khong duoc mat hang vinh vien")
    void cancellingOrderMustRestoreStock() throws Exception {
        Long orderId = createOrder(3);

        Book afterOrder = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(afterOrder.getQuantity())
                .as("dat 3 cuon thi ton kho phai giam con 7")
                .isEqualTo(INITIAL_STOCK - 3);

        MvcResult cancelResult = updateStatus(orderId, StatusEnum.CANCEL);
        assertThat(cancelResult.getResponse().getStatus())
                .as("body=%s", cancelResult.getResponse().getContentAsString())
                .isEqualTo(200);

        Book afterCancel = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(afterCancel.getQuantity())
                .as("huy don phai tra 3 cuon ve kho")
                .isEqualTo(INITIAL_STOCK);
        assertThat(afterCancel.getSold())
                .as("so da ban phai tru lai ve 0")
                .isEqualTo("0");
    }

    @Test
    @DisplayName("PRICE: server tu tinh totalPrice, khong tin gia tri client gui")
    void totalPriceMustBeComputedOnServer() throws Exception {
        Long orderId = createOrderWithClaimedTotal(2, 0.0);

        Order saved = orderRepository.findById(orderId).orElseThrow();

        // 2 x 120.000 = 240.000 >= nguong 150.000 nen mien phi ship
        assertThat(saved.getTotalPrice())
                .as("client gui totalPrice=0 nhung server phai tu tinh lai")
                .isEqualTo(2 * BOOK_PRICE);
    }

    @Test
    @DisplayName("PRICE: don duoi nguong mien phi phai duoc cong phi van chuyen")
    void totalPriceMustIncludeShippingFeeBelowThreshold() throws Exception {
        Book cheapBook = bookRepository.findById(book.getId()).orElseThrow();
        cheapBook.setPrice(50_000.0);
        bookRepository.save(cheapBook);

        Long orderId = createOrderWithClaimedTotal(1, 999.0);
        Order saved = orderRepository.findById(orderId).orElseThrow();

        assertThat(saved.getTotalPrice())
                .as("50.000 < 150.000 nen phai cong them 30.000 phi ship")
                .isEqualTo(50_000.0 + 30_000.0);
    }

    @Test
    @DisplayName("STATUS: khong duoc mo lai don da huy")
    void cancelledOrderMustNotBeReopened() throws Exception {
        Long orderId = createOrder(1);
        updateStatus(orderId, StatusEnum.CANCEL);

        MvcResult result = updateStatus(orderId, StatusEnum.PENDING);

        assertThat(result.getResponse().getStatus())
                .as("CANCEL la trang thai ket thuc, body=%s", result.getResponse().getContentAsString())
                .isBetween(400, 499);

        Book afterReopenAttempt = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(afterReopenAttempt.getQuantity())
                .as("bi tu choi thi ton kho phai giu nguyen")
                .isEqualTo(INITIAL_STOCK);
    }

    @Test
    @DisplayName("STATUS: luong hop le PENDING -> SHIPPING -> SUCCESS van chay binh thuong")
    void validStatusFlowMustBeAllowed() throws Exception {
        Long orderId = createOrder(1);

        assertThat(updateStatus(orderId, StatusEnum.SHIPPING).getResponse().getStatus()).isEqualTo(200);
        assertThat(updateStatus(orderId, StatusEnum.SUCCESS).getResponse().getStatus()).isEqualTo(200);

        Book afterDelivery = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(afterDelivery.getQuantity())
                .as("giao hang thanh cong thi khong hoan kho")
                .isEqualTo(INITIAL_STOCK - 1);
    }

    private Long createOrder(int quantity) throws Exception {
        return createOrderWithClaimedTotal(quantity, quantity * BOOK_PRICE);
    }

    private Long createOrderWithClaimedTotal(int quantity, double claimedTotal) throws Exception {
        String payload = "{"
                + "\"name\":\"Nguyen Van A\","
                + "\"email\":\"buyer@bookshop.local\","
                + "\"phone\":\"0912345678\","
                + "\"address\":\"So 1 Dai Co Viet, Hai Ba Trung, Ha Noi\","
                + "\"paymentMethod\":\"COD\","
                + "\"totalPrice\":" + claimedTotal + ","
                + "\"status\":\"PENDING\","
                + "\"detail\":[{\"quantity\":" + quantity
                + ",\"bookName\":\"So Do\",\"book\":{\"id\":" + book.getId() + "}}]"
                + "}";

        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .as("tao don that bai, body=%s", result.getResponse().getContentAsString())
                .isEqualTo(201);

        return orderRepository.findAll().stream()
                .map(Order::getId)
                .max(Long::compareTo)
                .orElseThrow();
    }

    private MvcResult updateStatus(Long orderId, StatusEnum status) throws Exception {
        return mockMvc.perform(patch("/api/v1/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"" + status.name() + "\"}"))
                .andReturn();
    }
}
