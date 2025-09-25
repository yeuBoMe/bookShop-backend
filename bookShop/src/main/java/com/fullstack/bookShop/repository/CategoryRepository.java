package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    // check exists
    boolean existsByName(String name);
}
