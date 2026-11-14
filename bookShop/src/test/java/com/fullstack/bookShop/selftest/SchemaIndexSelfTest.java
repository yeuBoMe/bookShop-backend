package com.fullstack.bookShop.selftest;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Doc thang INFORMATION_SCHEMA de chung minh index khai bang @Index tren entity
 * that su duoc tao ra, thay vi tin rang "test xanh nghia la co".
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SchemaIndexSelfTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("SCHEMA: moi @Index khai tren entity deu ton tai trong database")
    void declaredEntityIndexesMustExist() {
        Set<String> actual = readIndexNames();

        List<String> expected = List.of(
                "idx_books_category_id",
                "idx_books_created_at_id",
                "idx_books_price_id",
                "idx_books_average_stars_id",
                "idx_books_category_price_id",
                "idx_books_category_created_at_id",
                "idx_books_name",
                "idx_categories_created_at_id",
                "idx_categories_name",
                "idx_orders_created_at_id",
                "idx_orders_email_created_at",
                "idx_orders_status_created_at",
                "idx_order_details_order_id",
                "idx_order_details_book_id",
                "idx_users_role_id",
                "idx_users_created_at_id",
                "idx_users_full_name",
                "idx_ratings_book_id_stars",
                "idx_ratings_user_id",
                "idx_ratings_created_at_id",
                "idx_roles_name",
                "idx_permissions_name");

        assertThat(actual)
                .as("index thuc te trong DB: %s", actual)
                .containsAll(expected.stream().map(name -> name.toLowerCase(Locale.ROOT)).toList());
    }

    @SuppressWarnings("unchecked")
    private Set<String> readIndexNames() {
        List<Object> rows = entityManager
                .createNativeQuery("SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES")
                .getResultList();

        return rows.stream()
                .map(String::valueOf)
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }
}
