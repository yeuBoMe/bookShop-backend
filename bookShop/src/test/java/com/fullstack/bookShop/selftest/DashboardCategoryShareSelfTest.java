package com.fullstack.bookShop.selftest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Ty trong doanh thu theo the loai - so lieu cua bieu do tron.
 *
 * <p>Tat cache trong lop test nay: cache con han thi test sau doc lai ket qua cua test truoc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "bookshop.cache.enabled=false")
@WithMockUser(username = "admin@bookshop.local", roles = {"ADMIN"})
class DashboardCategoryShareSelfTest {

    private static final String ENDPOINT = "/api/v1/dashboard/revenue-by-category";

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @BeforeEach
    void clean() {
        orderDetailRepository.deleteAll();
        orderRepository.deleteAll();
        ratingRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("BIEU DO TRON: ty trong tinh dung va cac lat cong lai bang 100 phan tram")
    void sharesMustMatchRevenueAndSumToOneHundred() throws Exception {
        Book vanHoc = saveBook("Sach Van Hoc", "Tac Gia A", 100_000.0, saveCategory("Van hoc"));
        Book kinhTe = saveBook("Sach Kinh Te", "Tac Gia B", 100_000.0, saveCategory("Kinh te"));

        // Van hoc 7 cuon x 100.000 = 700.000 (70%) | Kinh te 3 cuon x 100.000 = 300.000 (30%)
        saveOrder(StatusEnum.SUCCESS, vanHoc, 7);
        saveOrder(StatusEnum.PAID, kinhTe, 3);

        String body = mockMvc.perform(get(ENDPOINT).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data[0].name").value("Van hoc"))
                .andExpect(jsonPath("$.data[0].revenue").value(700_000.0))
                .andExpect(jsonPath("$.data[0].percentage").value(70.0))
                .andExpect(jsonPath("$.data[1].name").value("Kinh te"))
                .andExpect(jsonPath("$.data[1].percentage").value(30.0))
                .andReturn().getResponse().getContentAsString();

        assertThat(sumPercentage(body)).as("tong cac lat cat, body=%s", body).isEqualTo(100.0);
    }

    @Test
    @DisplayName("BIEU DO TRON: don chua thanh toan hoac da huy khong duoc tinh vao ty trong")
    void unpaidOrdersMustNotAffectShares() throws Exception {
        Book vanHoc = saveBook("Sach Van Hoc", "Tac Gia A", 100_000.0, saveCategory("Van hoc"));
        Book kinhTe = saveBook("Sach Kinh Te", "Tac Gia B", 100_000.0, saveCategory("Kinh te"));

        saveOrder(StatusEnum.SUCCESS, vanHoc, 5);
        saveOrder(StatusEnum.PENDING, kinhTe, 99);
        saveOrder(StatusEnum.CANCEL, kinhTe, 99);

        mockMvc.perform(get(ENDPOINT).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Van hoc"))
                .andExpect(jsonPath("$.data[0].percentage").value(100.0));
    }

    @Test
    @DisplayName("BIEU DO TRON: sach chua gan the loai van phai co lat cat rieng, khong bi bo qua")
    void booksWithoutCategoryMustStillAppear() throws Exception {
        Book vanHoc = saveBook("Sach Van Hoc", "Tac Gia A", 100_000.0, saveCategory("Van hoc"));
        Book khongTheLoai = saveBook("Sach Le", "Tac Gia C", 100_000.0, null);

        saveOrder(StatusEnum.SUCCESS, vanHoc, 5);
        saveOrder(StatusEnum.SUCCESS, khongTheLoai, 5);

        String body = mockMvc.perform(get(ENDPOINT).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andReturn().getResponse().getContentAsString();

        assertThat(sumPercentage(body))
                .as("bo qua sach chua phan loai la tong khong con du, body=%s", body)
                .isEqualTo(100.0);
    }

    @Test
    @DisplayName("BIEU DO TRON: qua nhieu the loai thi gop phan duoi vao mot lat Khac")
    void extraCategoriesMustCollapseIntoOthers() throws Exception {
        // topLimit = 2 -> 2 lat lon nhat + 1 lat gop
        for (int index = 1; index <= 4; index++) {
            // Ten tac gia khong duoc chua chu so (rang buoc @Pattern cua Book)
            String author = "Tac Gia " + (char) ('A' + index);
            Book book = saveBook("Sach " + index, author, 100_000.0, saveCategory("The loai " + index));
            saveOrder(StatusEnum.SUCCESS, book, 5 - index);
        }

        String body = mockMvc.perform(get(ENDPOINT).param("topLimit", "2").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andReturn().getResponse().getContentAsString();

        assertThat(sumPercentage(body)).as("body=%s", body).isEqualTo(100.0);
        assertThat(objectMapper.readTree(body).path("data").get(2).path("categoryId").isNull())
                .as("lat gop khong thuoc the loai nao nen categoryId phai rong")
                .isTrue();
    }

    @Test
    @DisplayName("BIEU DO TRON: chua co doanh thu thi tra danh sach rong, khong chia cho 0")
    void emptyRevenueMustReturnEmptyList() throws Exception {
        mockMvc.perform(get(ENDPOINT).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("BIEU DO TRON: user thuong khong duoc xem so lieu doanh thu")
    @WithMockUser(username = "normal@bookshop.local", roles = {"USER"})
    void normalUserMustNotSeeRevenueShare() throws Exception {
        int status = mockMvc.perform(get(ENDPOINT)).andReturn().getResponse().getStatus();
        assertThat(status).isIn(401, 403);
    }

    private double sumPercentage(String body) throws Exception {
        double total = 0.0;
        for (JsonNode slice : objectMapper.readTree(body).path("data")) {
            total += slice.path("percentage").asDouble();
        }
        return Math.round(total * 10.0) / 10.0;
    }

    private Category saveCategory(String name) {
        Category category = new Category();
        category.setName(name);
        return categoryRepository.save(category);
    }

    private Book saveBook(String name, String author, double price, Category category) {
        Book book = new Book();
        book.setName(name);
        book.setAuthor(author);
        book.setPrice(price);
        book.setQuantity(100);
        book.setIntroduceText("Gioi thieu");
        book.setDescription("Mo ta");
        book.setReader("Doc gia");
        book.setCategory(category);
        return bookRepository.save(book);
    }

    private void saveOrder(StatusEnum status, Book book, int quantity) {
        Order order = new Order();
        order.setName("Khach Hang");
        order.setEmail("khach@bookshop.local");
        order.setPhone("0912345678");
        order.setAddress("So 1 Dai Co Viet, Hai Ba Trung, Ha Noi");
        order.setPaymentMethod("COD");
        order.setTotalPrice(book.getPrice() * quantity);
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
