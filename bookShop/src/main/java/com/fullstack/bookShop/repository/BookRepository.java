package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.domain.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    // check exists
    boolean existsByNameAndAuthor(String name, String author);

    // delete by category id
    void deleteByCategoryId(Long categoryId);
}
