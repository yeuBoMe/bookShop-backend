package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.domain.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long>, JpaSpecificationExecutor<Rating> {

    // find by book
    Page<Rating> findAllByBookId(Long bookId, Pageable pageable);

    // find by user and book
    Optional<Rating> findByUserIdAndBookId(Long userId, Long bookId);

    // delete by user, book
    void deleteByUserId(Long userId);

    void deleteByBookId(Long bookId);
}
