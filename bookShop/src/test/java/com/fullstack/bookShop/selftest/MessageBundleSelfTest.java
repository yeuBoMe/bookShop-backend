package com.fullstack.bookShop.selftest;

import com.fullstack.bookShop.repository.BookRepository;
import com.fullstack.bookShop.repository.CategoryRepository;
import com.fullstack.bookShop.repository.OrderDetailRepository;
import com.fullstack.bookShop.repository.OrderRepository;
import com.fullstack.bookShop.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Chung minh message that su duoc resolve tu bundle.
 * Can thiet vi setUseCodeAsDefaultMessage(true) lam key thieu van tra ve chinh key ma khong bao loi,
 * nen test xanh chua du de ket luan i18n chay dung.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "admin@bookshop.local", roles = {"ADMIN"})
class MessageBundleSelfTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceBundleMessageSource messageSource;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @BeforeEach
    void clean() {
        orderDetailRepository.deleteAll();
        orderRepository.deleteAll();
        ratingRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("I18N: @ApiMessage phai tra ve cau chu, khong duoc lo key ra ngoai")
    void apiMessageMustBeResolved() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/books")
                        .param("page", "1")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body)
                .as("phai la cau chu, body=%s", body)
                .contains("Fetch all books")
                .doesNotContain("api.book.fetch-all");
    }

    @Test
    @DisplayName("I18N: Accept-Language vi phai doi sang tieng Viet")
    void apiMessageMustSwitchToVietnamese() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/books")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "vi")
                        .param("page", "1")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body)
                .as("body=%s", body)
                .contains("Lấy danh sách sách thành công");
    }

    @Test
    @DisplayName("I18N: loi validation phai ra cau chu, khong ra {book.name.required}")
    void validationMessageMustBeResolved() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body)
                .as("body=%s", body)
                .doesNotContain("{book.")
                .doesNotContain("book.name.required");
        assertThat(body)
                .as("phai la cau chu doc duoc, body=%s", body)
                .contains("required");
    }

    @Test
    @DisplayName("I18N: loi nghiep vu phai dien tham so vao {0} thay vi in ra nguyen key")
    void businessErrorMustInterpolateArguments() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/books/{id}", 999_999))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body)
                .as("body=%s", body)
                .contains("Book not found with ID: 999999")
                .doesNotContain("book.error.id-not-found");
    }

    @Test
    @DisplayName("I18N: bundle vi phai co du moi key cua bundle mac dinh")
    void vietnameseBundleMustCoverAllKeys() throws Exception {
        Properties defaultBundle = loadBundle("messages.properties");
        Properties vietnameseBundle = loadBundle("messages_vi.properties");

        List<String> missing = new ArrayList<>();
        for (String key : defaultBundle.stringPropertyNames()) {
            if (!vietnameseBundle.containsKey(key)) {
                missing.add(key);
            }
        }

        assertThat(missing)
                .as("cac key chua dich sang tieng Viet: %s", missing)
                .isEmpty();
    }

    @Test
    @DisplayName("I18N: moi key trong bundle deu resolve duoc (khong tra ve chinh key)")
    void everyKeyMustResolve() throws Exception {
        Properties defaultBundle = loadBundle("messages.properties");

        List<String> unresolved = new ArrayList<>();
        for (String key : defaultBundle.stringPropertyNames()) {
            String resolved = messageSource.getMessage(key, null, Locale.ENGLISH);
            if (key.equals(resolved)) {
                unresolved.add(key);
            }
        }

        assertThat(unresolved)
                .as("key khong resolve duoc (bundle khong nap dung?): %s", unresolved)
                .isEmpty();
    }

    private Properties loadBundle(String fileName) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertThat(input).as("khong tim thay %s tren classpath", fileName).isNotNull();
            properties.load(new java.io.InputStreamReader(input, java.nio.charset.StandardCharsets.UTF_8));
        }
        return properties;
    }
}
