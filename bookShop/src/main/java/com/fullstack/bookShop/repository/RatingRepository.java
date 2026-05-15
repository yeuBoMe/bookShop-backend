package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.dto.response.BookAverageRatingResponse;
import com.fullstack.bookShop.model.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long>, JpaSpecificationExecutor<Rating> {

    // find by book
    Page<Rating> findAllByBookId(Long bookId, Pageable pageable);

    // find by user and book
    Optional<Rating> findByUserIdAndBookId(Long userId, Long bookId);

    // Calculate average rating of book
    @Query("SELECT COALESCE(AVG(r.stars), 0) FROM Rating r WHERE r.book.id = :bookId")
    Double getAverageRatingByBookId(@Param(("bookId")) Long bookId);

    @Query("""
        SELECT 
           r.book.id AS bookId,
           COALESCE(AVG(r.stars), 0) AS averageStars    
        FROM Rating r
        WHERE r.book.id IN :bookIds
        GROUP BY r.book.id        
    """)
    List<BookAverageRatingResponse> getAverageRatingsByBookIds(@Param("bookIds") Set<Long> bookIds);

    // delete by user, book
    void deleteByUserId(Long userId);

    void deleteByBookId(Long bookId);
}
