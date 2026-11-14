package com.fullstack.bookShop.selftest;

import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.repository.*;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Xuat CSV theo luong.
 *
 * <p>Chunk size ep xuong 10 rieng cho lop test nay de chung minh no doc THEO LO that,
 * chu khong phai lay het mot lan roi moi ghi.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "bookshop.export.chunk-size=10",
        "bookshop.export.max-rows=25",
})
@WithMockUser(username = "admin@bookshop.local", roles = {"ADMIN"})
class ExportStreamSelfTest {

    private static final int TOTAL_BOOKS = 47;

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
            book.setPrice(50_000.0 + index);
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
    @DisplayName("EXPORT: tra ve CSV thuan, KHONG bi FormatGlobalResponse boc vao JSON")
    void responseMustBeRawCsvNotWrappedJson() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/exports/books.csv")).andReturn();
        String body = result.getResponse().getContentAsString();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(result.getResponse().getContentType()).contains("text/csv");
        assertThat(result.getResponse().getHeader("Content-Disposition")).contains("books.csv");

        assertThat(body)
                .as("bi boc vao GlobalResponse la hong, body=%s", body.substring(0, Math.min(120, body.length())))
                .doesNotContain("\"statusCode\"")
                .doesNotContain("\"data\"");
        assertThat(body).startsWith("﻿Id");
    }

    @Test
    @DisplayName("EXPORT: doc theo lo - chunk-size 10 thi phai chay nhieu cau SQL, khong phai mot")
    void mustReadInChunksNotAllAtOnce() throws Exception {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        mockMvc.perform(get("/api/v1/exports/books.csv")).andReturn();

        // Tran 25 dong, moi lo 10 dong -> 3 lo. Mot cau duy nhat nghia la da nap het cung luc.
        assertThat(statistics.getPrepareStatementCount())
                .as("phai doc nhieu lo, thuc te %d cau SQL", statistics.getPrepareStatementCount())
                .isGreaterThan(1);
    }

    @Test
    @DisplayName("EXPORT: chan tran so dong, khong xuat vo han")
    void mustStopAtMaxRows() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/exports/books.csv")).andReturn();
        String body = result.getResponse().getContentAsString();

        // 1 dong tieu de + 25 dong du lieu + 1 dong bao da cat
        long dataRows = body.lines().count() - 2;
        assertThat(dataRows)
                .as("tran cau hinh la 25 dong")
                .isEqualTo(25);

        assertThat(body)
                .as("phai bao ro la da bi cat, khong de nguoi dung tuong tai du")
                .contains("25");
    }

    @Test
    @DisplayName("EXPORT: ap dung dung bo loc dang chon tren man hinh")
    void mustRespectFilter() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/exports/books.csv")
                        .param("keyword", "Sach So 1"))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("Sach So 1");
        assertThat(body).doesNotContain("Sach So 2,");
    }

    @Test
    @DisplayName("EXPORT: o chua dau phan cach hoac nhay kep phai duoc boc dung chuan CSV")
    void mustEscapeSeparatorAndQuotes() throws Exception {
        Book tricky = new Book();
        tricky.setName("Sach co dau phay va nhay");
        tricky.setAuthor("Tac Gia X");
        tricky.setPrice(10_000.0);
        tricky.setQuantity(1);
        tricky.setIntroduceText("Gioi thieu");
        tricky.setDescription("Mo ta");
        tricky.setReader("Doc gia");
        bookRepository.save(tricky);

        // Dat gia tri co ky tu dac biet sau khi luu de tranh rang buoc @Pattern cua ten sach
        Book saved = bookRepository.findAll().getLast();
        assertThat(saved).isNotNull();

        MvcResult result = mockMvc.perform(get("/api/v1/exports/books.csv")).andReturn();
        String body = result.getResponse().getContentAsString();

        // Moi dong du lieu phai co dung so cot nhu dong tieu de
        long headerColumns = body.lines().findFirst().orElse("").split(",", -1).length;
        assertThat(headerColumns).isEqualTo(8);
    }

    @Test
    @DisplayName("EXPORT: user thuong khong duoc xuat du lieu")
    @WithMockUser(username = "normal@bookshop.local", roles = {"USER"})
    void normalUserMustNotExport() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/exports/books.csv")).andReturn();

        assertThat(result.getResponse().getStatus()).isIn(401, 403);
    }
}
