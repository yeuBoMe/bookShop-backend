package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.domain.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    // check exists
    @Query("SELECT COUNT(b) > 0 FROM Book b " +
            "WHERE LOWER(b.name) = LOWER(:name) " +
            "AND LOWER(b.author) = LOWER(:author)"
    )
    boolean existsByNameAndAuthorIgnoreCase(@Param("name") String name, @Param("author") String author);

    // delete by category id
    void deleteByCategoryId(Long categoryId);
}
