package com.fullstack.bookShop.selftest;

import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sinh id bang SEQUENCE voi allocationSize 50.
 *
 * <p>Optimizer "pooled" mac dinh cua Hibernate coi gia tri nextval tra ve la CAN TREN
 * cua khoi id. Sequence moi tao bat dau tu 1 nen khoi dau tien la -48..1: DATABASE MOI
 * SE SINH ID AM. Da chuyen sang "pooled-lo" (coi la can duoi) - test nay chan hoi quy.
 */
@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(username = "admin@bookshop.local", roles = {"ADMIN"})
class SequenceIdSelfTest {

    private static final int SAMPLE_SIZE = 120;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @BeforeEach
    void clean() {
        // Phai don theo dung thu tu khoa ngoai, khong thi H2 tu choi xoa books
        orderDetailRepository.deleteAll();
        orderRepository.deleteAll();
        ratingRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("ID: khong duoc sinh id am hay id 0")
    void generatedIdsMustBePositive() {
        List<Category> categories = new ArrayList<>();
        for (int index = 1; index <= SAMPLE_SIZE; index++) {
            Category category = new Category();
            category.setName("The loai " + index);
            categories.add(category);
        }

        List<Category> saved = categoryRepository.saveAll(categories);

        List<Long> nonPositiveIds = saved.stream()
                .map(Category::getId)
                .filter(id -> id == null || id <= 0)
                .toList();

        assertThat(nonPositiveIds)
                .as("id am hoac 0 xuat hien: %s", nonPositiveIds)
                .isEmpty();
    }

    @Test
    @DisplayName("ID: khong duoc trung nhau khi luu vuot qua mot khoi allocation")
    void generatedIdsMustBeUniqueAcrossAllocationBlocks() {
        List<Category> categories = new ArrayList<>();
        // 120 > allocationSize 50 nen chac chan phai xin sang khoi thu ba
        for (int index = 1; index <= SAMPLE_SIZE; index++) {
            Category category = new Category();
            category.setName("Khoi " + index);
            categories.add(category);
        }

        List<Long> ids = categoryRepository.saveAll(categories)
                .stream()
                .map(Category::getId)
                .toList();

        assertThat(ids).doesNotHaveDuplicates();
        assertThat(ids).hasSize(SAMPLE_SIZE);
    }
}
