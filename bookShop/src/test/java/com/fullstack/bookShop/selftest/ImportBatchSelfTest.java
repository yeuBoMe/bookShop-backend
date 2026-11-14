package com.fullstack.bookShop.selftest;

import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Category;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Endpoint import moi: moi dong mang thao tac rieng va duoc tinh dung/sai doc lap.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "admin@bookshop.local", roles = {"ADMIN"})
class ImportBatchSelfTest {

    private static final String BATCH_ENDPOINT = "/api/v1/books/import/batch";

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
    @DisplayName("BATCH: mot dong hong khong lam hong ca file")
    void oneBadRowMustNotFailWholeFile() throws Exception {
        String payload = "["
                + row("CREATE", "Sach Mot", "Tac Gia A") + ","
                + "{\"action\":\"CREATE\",\"name\":\"\",\"author\":\"Tac Gia B\"}" + ","
                + row("CREATE", "Sach Ba", "Tac Gia C")
                + "]";

        mockMvc.perform(post(BATCH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(jsonPath("$.data.countSuccess").value(2))
                .andExpect(jsonPath("$.data.countError").value(1))
                .andExpect(jsonPath("$.data.detail[0].index").value(2));

        assertThat(bookRepository.count())
                .as("hai dong hop le phai duoc luu that")
                .isEqualTo(2);
    }

    @Test
    @DisplayName("BATCH: CREATE, UPDATE, DELETE chay duoc trong cung mot file")
    void createUpdateDeleteMustWorkTogether() throws Exception {
        mockMvc.perform(post(BATCH_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("[" + row("CREATE", "Sach Cu", "Tac Gia X") + ","
                        + row("CREATE", "Sach Xoa", "Tac Gia Y") + "]"))
                .andReturn();

        assertThat(bookRepository.count()).isEqualTo(2);

        String payload = "["
                + row("CREATE", "Sach Moi", "Tac Gia Z") + ","
                + "{\"action\":\"UPDATE\",\"name\":\"Sach Cu\",\"author\":\"Tac Gia X\","
                + "\"introduceText\":\"Gioi thieu\",\"description\":\"Mo ta\",\"reader\":\"Doc gia\","
                + "\"price\":99000,\"quantity\":7,\"category\":\"Van hoc\"}" + ","
                + "{\"action\":\"DELETE\",\"name\":\"Sach Xoa\",\"author\":\"Tac Gia Y\"}"
                + "]";

        mockMvc.perform(post(BATCH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(jsonPath("$.data.countSuccess").value(3))
                .andExpect(jsonPath("$.data.countError").value(0));

        Book updated = bookRepository.findByNameAndAuthorIgnoreCase("Sach Cu", "Tac Gia X").orElseThrow();
        assertThat(updated.getQuantity()).as("UPDATE phai doi so luong").isEqualTo(7);
        assertThat(updated.getPrice()).as("UPDATE phai doi gia").isEqualTo(99000.0);

        assertThat(bookRepository.findByNameAndAuthorIgnoreCase("Sach Xoa", "Tac Gia Y"))
                .as("DELETE phai xoa that")
                .isEmpty();
        assertThat(bookRepository.findByNameAndAuthorIgnoreCase("Sach Moi", "Tac Gia Z"))
                .as("CREATE phai tao that")
                .isPresent();
    }

    @Test
    @DisplayName("BATCH: DELETE chi can khoa dinh danh, khong bat khai du cot")
    void deleteRowMustNotRequireAllColumns() throws Exception {
        mockMvc.perform(post(BATCH_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("[" + row("CREATE", "Sach Don", "Tac Gia D") + "]")).andReturn();

        mockMvc.perform(post(BATCH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"action\":\"DELETE\",\"name\":\"Sach Don\",\"author\":\"Tac Gia D\"}]"))
                .andExpect(jsonPath("$.data.countSuccess").value(1))
                .andExpect(jsonPath("$.data.countError").value(0));

        assertThat(bookRepository.count()).isZero();
    }

    @Test
    @DisplayName("BATCH: trung khoa trong cung file thi chi dong sau bi tu choi")
    void duplicatedKeyInFileMustRejectOnlyLaterRow() throws Exception {
        String payload = "["
                + row("CREATE", "Sach Trung", "Tac Gia E") + ","
                + row("CREATE", "Sach Trung", "Tac Gia E")
                + "]";

        mockMvc.perform(post(BATCH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(jsonPath("$.data.countSuccess").value(1))
                .andExpect(jsonPath("$.data.countError").value(1))
                .andExpect(jsonPath("$.data.detail[0].index").value(2));

        assertThat(bookRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("BATCH: UPDATE ban ghi khong ton tai bao loi dung dong, dong khac van chay")
    void updateMissingRecordMustFailOnlyThatRow() throws Exception {
        String payload = "["
                + row("CREATE", "Sach Ton Tai", "Tac Gia F") + ","
                + "{\"action\":\"UPDATE\",\"name\":\"Sach Khong Co\",\"author\":\"Tac Gia G\","
                + "\"introduceText\":\"Gioi thieu\",\"description\":\"Mo ta\",\"reader\":\"Doc gia\","
                + "\"price\":10000,\"quantity\":1,\"category\":\"Van hoc\"}"
                + "]";

        MvcResult result = mockMvc.perform(post(BATCH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(jsonPath("$.data.countSuccess").value(1))
                .andExpect(jsonPath("$.data.countError").value(1))
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .as("phai noi ro khong tim thay ban ghi nao")
                .contains("not found");
    }

    /**
     * Ghim hanh vi HIEN TAI cua endpoint cu de biet neu no vo tinh bi doi, KHONG phai de tan thanh.
     * Mot dong sai la ca file bi tu choi va khong dong nao vao duoc DB - dung cai ma
     * endpoint /import/batch sinh ra de khac phuc.
     */
    @Test
    @DisplayName("REGRESSION: endpoint /import cu van tu choi ca file khi mot dong sai")
    void legacyImportEndpointStillRejectsWholeFile() throws Exception {
        String payload = "[{\"name\":\"\",\"author\":\"Tac Gia H\"}]";

        MvcResult result = mockMvc.perform(post("/api/v1/books/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .as("body=%s", result.getResponse().getContentAsString())
                .isGreaterThanOrEqualTo(400);
        assertThat(bookRepository.count())
                .as("ca file bi tu choi nen khong dong nao duoc luu")
                .isZero();
    }

    private String row(String action, String name, String author) {
        return "{"
                + "\"action\":\"" + action + "\","
                + "\"name\":\"" + name + "\","
                + "\"author\":\"" + author + "\","
                + "\"introduceText\":\"Gioi thieu\","
                + "\"description\":\"Mo ta chi tiet\","
                + "\"reader\":\"Doc gia pho thong\","
                + "\"price\":120000,"
                + "\"quantity\":10,"
                + "\"category\":\"Van hoc\""
                + "}";
    }
}
