package com.fullstack.bookShop.selftest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Phan trang theo con tro. Cho de sai nhat khong phai la "chay duoc" ma la
 * trang sau LAP LAI hoac BO SOT ban ghi khi nhieu dong co cung gia tri sap xep.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookSearchCursorSelfTest {

    private static final String ENDPOINT = "/api/v1/books/search";

    private static final int TOTAL_BOOKS = 47;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        final Category saved = categoryRepository.save(category);

        List<Book> books = new ArrayList<>();
        for (int index = 1; index <= TOTAL_BOOKS; index++) {
            Book book = new Book();
            book.setName("Sach So " + index);
            book.setAuthor("Tac Gia " + (char) ('A' + index % 26));
            // Cố ý để NHIỀU cuốn cùng giá: đây là chỗ keyset dễ lặp/sót nếu thiếu khoá phụ id
            book.setPrice(index % 5 == 0 ? 100_000.0 : 50_000.0);
            book.setQuantity(10);
            book.setIntroduceText("Gioi thieu");
            book.setDescription("Mo ta");
            book.setReader("Doc gia");
            book.setCategory(saved);
            books.add(book);
        }
        bookRepository.saveAll(books);
    }

    @Test
    @DisplayName("CURSOR: duyet het moi trang phai lay du va KHONG lap ban ghi nao")
    void pagingThroughCursorsMustCoverEveryRowExactlyOnce() throws Exception {
        Set<Integer> seenIds = new HashSet<>();
        String cursor = null;
        int pageCount = 0;

        do {
            JsonNode data = callSearch(builder -> {
                builder.param("size", "10").param("sort", "PRICE_ASC");
                return builder;
            }, cursor);

            for (JsonNode item : data.get("items")) {
                int id = item.get("id").asInt();
                assertThat(seenIds.add(id))
                        .as("ban ghi id=%d xuat hien o hai trang khac nhau", id)
                        .isTrue();
            }

            cursor = data.get("nextCursor").isNull() ? null : data.get("nextCursor").asText();
            pageCount++;
            assertThat(pageCount).as("khong duoc lap vo han").isLessThan(20);

        } while (cursor != null);

        assertThat(seenIds)
                .as("phai duyet du %d cuon sau %d trang", TOTAL_BOOKS, pageCount)
                .hasSize(TOTAL_BOOKS);
    }

    @Test
    @DisplayName("CURSOR: khong tra ve tong so ban ghi - do la muc dich cua keyset")
    void responseMustNotContainTotalCount() throws Exception {
        JsonNode data = callSearch(builder -> builder.param("size", "5"), null);

        assertThat(data.has("items")).isTrue();
        assertThat(data.has("nextCursor")).isTrue();
        assertThat(data.has("hasNext")).isTrue();
        assertThat(data.has("total")).as("khong duoc co truong total").isFalse();
        assertThat(data.has("meta")).as("khong dung khuon phan trang cu").isFalse();
    }

    @Test
    @DisplayName("SEARCH: tim khong dau van ra ket qua co dau")
    void searchMustIgnoreVietnameseDiacritics() throws Exception {
        Book book = new Book();
        book.setName("Tuổi Trẻ Đáng Giá Bao Nhiêu");
        book.setAuthor("Rosie Nguyễn");
        book.setPrice(90_000.0);
        book.setQuantity(5);
        book.setIntroduceText("Gioi thieu");
        book.setDescription("Mo ta");
        book.setReader("Doc gia");
        bookRepository.save(book);

        JsonNode data = callSearch(builder -> builder.param("keyword", "tuoi tre dang gia"), null);

        assertThat(data.get("items")).as("tim khong dau phai ra ket qua co dau").isNotEmpty();
        assertThat(data.get("items").get(0).get("name").asText()).isEqualTo("Tuổi Trẻ Đáng Giá Bao Nhiêu");
    }

    @Test
    @DisplayName("SEARCH: con tro bi sua bay khong duoc lam vo request")
    void tamperedCursorMustFallBackToFirstPage() throws Exception {
        JsonNode data = callSearch(builder -> builder.param("size", "5"), "khong-phai-base64-hop-le!!!");

        assertThat(data.get("items")).as("coi nhu khong co con tro, tra ve trang dau").hasSize(5);
    }

    @Test
    @DisplayName("SEARCH: size vuot tran bi keo ve muc toi da trong cau hinh")
    void oversizedPageMustBeCapped() throws Exception {
        JsonNode data = callSearch(builder -> builder.param("size", "99999"), null);

        assertThat(data.get("items").size())
                .as("size phai bi chan lai, khong duoc tra ca bang")
                .isLessThanOrEqualTo(100);
    }

    private JsonNode callSearch(
            java.util.function.Function<MockHttpServletRequestBuilder, MockHttpServletRequestBuilder> customizer,
            String cursor
    ) throws Exception {
        MockHttpServletRequestBuilder builder = get(ENDPOINT).accept(MediaType.APPLICATION_JSON);
        builder = customizer.apply(builder);
        if (cursor != null) {
            builder = builder.param("cursor", cursor);
        }

        String body = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("data");
    }
}
