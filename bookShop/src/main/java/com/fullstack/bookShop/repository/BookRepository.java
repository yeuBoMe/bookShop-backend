package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    // check exists
    @Query("""
        SELECT COUNT(b) > 0 FROM Book b
        WHERE LOWER(b.name) = LOWER(:name)
        AND LOWER(b.author) = LOWER(:author)
    """)
    boolean existsByNameAndAuthorIgnoreCase(@Param("name") String name, @Param("author") String author);

    @Query("""
         SELECT LOWER(CONCAT(b.name, '_', b.author))
         FROM Book b 
         WHERE LOWER(CONCAT(b.name, '_', b.author)) IN :keys
    """)
    Set<String> findExistingNamesAndAuthorsIgnoreCase(@Param("keys") Set<String> keys);

    // Find list book with average rating filter
    @Query("""
        SELECT b FROM Book b
        LEFT JOIN Rating r ON r.book.id = b.id
        GROUP BY b.id
        HAVING COALESCE(AVG(r.stars), 0) >= :minRating
        ORDER BY COALESCE(AVG(r.stars), 0) DESC
    """)
    Page<Book> findAllByMinRatingGreaterThanEqual(@Param("minRating") Double minRating, Pageable pageable);

    // Find all books by book ids
    @Query("SELECT b FROM Book b WHERE b.id IN :bookIds")
    List<Book> findAllByBookIds(@Param("bookIds") Set<Long> bookIds);

    // delete by category id
    void deleteByCategoryId(Long categoryId);
}
