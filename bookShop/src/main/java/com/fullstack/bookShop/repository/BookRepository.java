package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.domain.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    // check exists
    @Query("""
        SELECT COUNT(b) > 0 FROM Book b
        WHERE LOWER(b.name) = LOWER(:name)
        AND LOWER(b.author) = LOWER(:author)
    """)
    boolean existsByNameAndAuthorIgnoreCase(@Param("name") String name, @Param("author") String author);

    // Find list book with average rating filter
    @Query("""
        SELECT b FROM Book b
        LEFT JOIN Rating r ON r.book.id = b.id
        GROUP BY b.id
        HAVING COALESCE(AVG(r.stars), 0) >= :minRating
        ORDER BY COALESCE(AVG(r.stars), 0) DESC
    """)
    Page<Book> findAllByMinRatingGreaterThanEqual(@Param("minRating") Double minRating, Pageable pageable);

    // delete by category id
    void deleteByCategoryId(Long categoryId);
}
