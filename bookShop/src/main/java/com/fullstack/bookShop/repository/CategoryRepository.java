package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    // check exists
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE LOWER(c.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);
}
