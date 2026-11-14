package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.dto.response.BookRatingSummaryResponse;
import com.fullstack.bookShop.model.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long>, JpaSpecificationExecutor<Rating> {

    // Nap kem user + book trong 1 cau: neu de lazy/eager mac dinh se sinh N+1 khi map response
    @Override
    @EntityGraph(attributePaths = {"user", "book"})
    Page<Rating> findAll(Specification<Rating> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "book"})
    Page<Rating> findAllByBookId(Long bookId, Pageable pageable);

    // find by user and book
    Optional<Rating> findByUserIdAndBookId(Long userId, Long bookId);

    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    long countByBookId(Long bookId);

    /**
     * Diem trung binh + so luot danh gia cua NHIEU sach trong mot cau.
     *
     * <p>Sach khong con danh gia nao se khong co dong nao trong ket qua,
     * ben goi phai tu dat ve 0 - xem {@code BookRatingSummaryUpdater}.
     */
    @Query("""
        SELECT
            r.book.id                 AS bookId,
            COALESCE(AVG(r.stars), 0) AS averageStars,
            COUNT(r.id)               AS ratingCount
        FROM Rating r
        WHERE r.book.id IN :bookIds
        GROUP BY r.book.id
    """)
    List<BookRatingSummaryResponse> getRatingSummariesByBookIds(@Param("bookIds") Set<Long> bookIds);

    /** Lay truoc danh sach sach bi anh huong de con tinh lai diem sau khi xoa danh gia. */
    @Query("""
        SELECT DISTINCT
            r.book.id
        FROM Rating r
        WHERE r.user.id = :userId
    """)
    Set<Long> findRatedBookIdsByUserId(@Param("userId") Long userId);

    // delete by user, book
    void deleteByUserId(Long userId);

    void deleteByBookId(Long bookId);
}
