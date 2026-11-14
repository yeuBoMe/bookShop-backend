package com.fullstack.bookShop.selftest;

import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.model.Rating;
import com.fullstack.bookShop.model.User;
import com.fullstack.bookShop.repository.*;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Self-test cac tinh nang dang co: chay that qua MockMvc de lo bug, khong ket luan tu doc code.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "selftest@bookshop.local", roles = {"ADMIN"})
class ExistingFeatureSelfTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    private Category vanHoc;

    private Book bookVanHoc;

    private User reader;

    @BeforeEach
    void seed() {
        orderDetailRepository.deleteAll();
        orderRepository.deleteAll();
        ratingRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();

        vanHoc = categoryRepository.save(newCategory("Van hoc"));
        Category kinhTe = categoryRepository.save(newCategory("Kinh te"));

        bookVanHoc = bookRepository.save(newBook("So Do", "Vu Trong Phung", 120000.0, vanHoc));
        bookRepository.save(newBook("Kinh Te Hoc", "Paul Samuelson", 250000.0, kinhTe));

        reader = userRepository.findByEmail("reader.selftest@bookshop.local")
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail("reader.selftest@bookshop.local");
                    user.setFullName("Reader Self Test");
                    user.setPassword("secret-hash");
                    return userRepository.save(user);
                });
    }

    @Test
    @DisplayName("BUG-A: /books/average-rating bo qua filter nen tra ve ca sach ngoai category duoc loc")
    void averageRatingEndpointMustRespectFilter() throws Exception {
        ratingRepository.save(newRating(bookVanHoc, reader, 5));

        MvcResult result = mockMvc.perform(get("/api/v1/books/average-rating")
                        .param("minRating", "0")
                        .param("filter", "category.name:'Van hoc'")
                        .param("page", "1")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(result.getResponse().getStatus())
                .as("endpoint phai tra 200, body=%s", body)
                .isEqualTo(200);
        assertThat(body)
                .as("filter category Van hoc phai loai sach Kinh te ra, body=%s", body)
                .doesNotContain("Kinh Te Hoc");
    }

    @Test
    @DisplayName("BUG-B: trang rong (page vuot qua trang cuoi) khong duoc tra 500")
    void emptyPageMustNotFail() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/books")
                        .param("page", "999")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .as("body=%s", result.getResponse().getContentAsString())
                .isEqualTo(200);
    }

    @Test
    @DisplayName("BUG-C: xoa anh tren sach chua co anh phai bao loi ro rang, khong phai 500")
    void removeImageOnBookWithoutImagesMustNotReturn500() throws Exception {
        MvcResult result = mockMvc.perform(delete("/api/v1/books/{id}/multiple-images", bookVanHoc.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"khong-ton-tai.png\"]"))
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .as("body=%s", result.getResponse().getContentAsString())
                .isLessThan(500);
    }

    @Test
    @DisplayName("BUG-D: danh gia trung sach phai tra loi nghiep vu, khong phai 500")
    void duplicateRatingMustReturnBusinessError() throws Exception {
        String payload = ratingPayload(reader.getId(), bookVanHoc.getId());

        mockMvc.perform(post("/api/v1/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)).andReturn();

        MvcResult second = mockMvc.perform(post("/api/v1/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andReturn();

        assertThat(second.getResponse().getStatus())
                .as("lan gui thu hai body=%s", second.getResponse().getContentAsString())
                .isLessThan(500);
    }

    @Test
    @DisplayName("BUG-E: tao don hang khong tru ton kho nen ban vuot so luong con lai")
    void createOrderMustDecreaseStock() throws Exception {
        int stockBefore = bookVanHoc.getQuantity();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderPayload("buyer.selftest@bookshop.local", "0912345678", 3, bookVanHoc.getId())))
                .andReturn();

        Book after = bookRepository.findById(bookVanHoc.getId()).orElseThrow();
        assertThat(after.getQuantity())
                .as("ton kho phai giam tu %d xuong %d sau khi ban 3 cuon", stockBefore, stockBefore - 3)
                .isEqualTo(stockBefore - 3);
    }

    @Test
    @DisplayName("BUG-F: dat mua nhieu hon ton kho phai bi tu choi")
    void createOrderExceedingStockMustBeRejected() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderPayload("buyer2.selftest@bookshop.local", "0912345679", 999999, bookVanHoc.getId())))
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .as("mua 999999 cuon khi ton kho chi con 10 phai bi tu choi, body=%s",
                        result.getResponse().getContentAsString())
                .isBetween(400, 499);
    }

    @Test
    @DisplayName("BUG-G: PATCH chi gui author bo qua kiem tra trung name + author")
    void partialUpdateMustStillRejectDuplicateNameAndAuthor() throws Exception {
        Book same = bookRepository.save(newBook("So Do", "Nguoi Khac", 90000.0, vanHoc));

        MvcResult result = mockMvc.perform(patch("/api/v1/books/{id}", same.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"author\":\"Vu Trong Phung\"}"))
                .andReturn();

        long duplicates = bookRepository.findAll().stream()
                .filter(book -> "So Do".equals(book.getName()) && "Vu Trong Phung".equals(book.getAuthor()))
                .count();

        assertThat(duplicates)
                .as("sau PATCH co %d ban ghi trung name+author, http status=%d",
                        duplicates, result.getResponse().getStatus())
                .isLessThanOrEqualTo(1);
    }

    @Test
    @DisplayName("SMOKE: cac endpoint doc chinh deu tra 2xx")
    void mainReadEndpointsRespond() throws Exception {
        List<String> endpoints = List.of(
                "/api/v1/books",
                "/api/v1/categories",
                "/api/v1/users",
                "/api/v1/orders",
                "/api/v1/ratings",
                "/api/v1/dashboard");

        for (String endpoint : endpoints) {
            MvcResult result = mockMvc.perform(get(endpoint)
                            .param("page", "1")
                            .param("size", "5")
                            .accept(MediaType.APPLICATION_JSON))
                    .andReturn();

            assertThat(result.getResponse().getStatus())
                    .as("%s tra ve body=%s", endpoint, result.getResponse().getContentAsString())
                    .isBetween(200, 299);
        }
    }

    private String ratingPayload(Long userId, Long bookId) {
        return "{\"stars\":5,\"comment\":\"Sach hay\",\"user\":{\"id\":" + userId
                + "},\"book\":{\"id\":" + bookId + "}}";
    }

    private String orderPayload(String email, String phone, int quantity, Long bookId) {
        return "{"
                + "\"name\":\"Nguyen Van A\","
                + "\"email\":\"" + email + "\","
                + "\"phone\":\"" + phone + "\","
                + "\"address\":\"So 1 Dai Co Viet, Hai Ba Trung, Ha Noi\","
                + "\"paymentMethod\":\"COD\","
                + "\"totalPrice\":120000,"
                + "\"status\":\"PENDING\","
                + "\"detail\":[{\"quantity\":" + quantity + ",\"bookName\":\"So Do\",\"book\":{\"id\":" + bookId + "}}]"
                + "}";
    }

    private Category newCategory(String name) {
        Category category = new Category();
        category.setName(name);
        return category;
    }

    private Book newBook(String name, String author, Double price, Category category) {
        Book book = new Book();
        book.setName(name);
        book.setAuthor(author);
        book.setPrice(price);
        book.setQuantity(10);
        book.setIntroduceText("Gioi thieu " + name);
        book.setDescription("Mo ta " + name);
        book.setReader("Doc gia pho thong");
        book.setCategory(category);
        return book;
    }

    private Rating newRating(Book book, User user, int stars) {
        Rating rating = new Rating();
        rating.setBook(book);
        rating.setUser(user);
        rating.setStars(stars);
        rating.setComment("Nhan xet tu self-test");
        return rating;
    }
}
