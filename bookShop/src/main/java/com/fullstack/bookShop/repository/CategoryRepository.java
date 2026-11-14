package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    // check exists
    @Query("""
        SELECT
            COUNT(c) > 0
        FROM Category c
        WHERE LOWER(c.name) = LOWER(:name)
    """)
    boolean existsByNameIgnoreCase(@Param("name") String name);

    // check set<name> exist
    @Query("""
        SELECT
            LOWER(c.name)
        FROM Category c
        WHERE LOWER(c.name) IN :names
    """)
    Set<String> findExistingNamesIgnoreCase(@Param("names") Set<String> names);

    // Tim theo ten, dung cho import UPDATE/DELETE
    @Query("""
        SELECT
            c
        FROM Category c
        WHERE LOWER(c.name) = LOWER(:name)
    """)
    Optional<Category> findByNameIgnoreCase(@Param("name") String name);

    @Query("""
        SELECT
            c
        FROM Category c
        WHERE LOWER(c.name) IN :names
    """)
    List<Category> findAllByNamesIgnoreCase(@Param("names") Set<String> names);
}
