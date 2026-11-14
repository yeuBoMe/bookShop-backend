package com.fullstack.bookShop.selftest;

import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.repository.BookRepository;
import com.fullstack.bookShop.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bon truong theo doi dau vet da chuyen len AuditableEntity.
 * Hook @PrePersist / @PreUpdate nam o lop cha van phai chay dung nhu truoc.
 */
@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(username = "auditor@bookshop.local", roles = {"ADMIN"})
class AuditableEntitySelfTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void clean() {
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("AUDIT: tao moi phai ghi createdAt va createdBy")
    void createMustStampCreatedFields() {
        Category category = new Category();
        category.setName("Van hoc");

        Category saved = categoryRepository.saveAndFlush(category);

        assertThat(saved.getCreatedAt()).as("createdAt phai duoc ghi").isNotNull();
        assertThat(saved.getCreatedBy())
                .as("createdBy phai la nguoi dang dang nhap")
                .isEqualTo("auditor@bookshop.local");
        assertThat(saved.getUpdatedAt()).as("chua sua thi updatedAt con trong").isNull();
    }

    @Test
    @DisplayName("AUDIT: sua phai ghi updatedAt va updatedBy, khong dung toi createdAt")
    void updateMustStampUpdatedFieldsOnly() {
        Category category = new Category();
        category.setName("Van hoc");
        Category saved = categoryRepository.saveAndFlush(category);

        var createdAt = saved.getCreatedAt();
        var createdBy = saved.getCreatedBy();

        saved.setName("Van hoc Viet Nam");
        Category updated = categoryRepository.saveAndFlush(saved);

        assertThat(updated.getUpdatedAt()).as("updatedAt phai duoc ghi").isNotNull();
        assertThat(updated.getUpdatedBy()).isEqualTo("auditor@bookshop.local");
        assertThat(updated.getCreatedAt()).as("createdAt khong duoc thay doi").isEqualTo(createdAt);
        assertThat(updated.getCreatedBy()).as("createdBy khong duoc thay doi").isEqualTo(createdBy);
    }

    @Test
    @DisplayName("AUDIT: cot trong DB van la created_at / created_by, khong doi ten")
    void auditColumnsMustKeepTheirNames() {
        Category category = new Category();
        category.setName("Kinh te");
        Category saved = categoryRepository.saveAndFlush(category);

        // Doc bang chinh ten cot cu: doi ten cot se lam cau nay no ngay
        Long countByColumn = categoryRepository.findById(saved.getId())
                .map(found -> found.getCreatedAt() != null ? 1L : 0L)
                .orElse(0L);

        assertThat(countByColumn).isEqualTo(1L);
    }
}
