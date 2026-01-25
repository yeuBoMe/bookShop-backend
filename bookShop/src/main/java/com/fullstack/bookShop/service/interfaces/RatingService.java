package com.fullstack.bookShop.service.interfaces;

import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.model.Rating;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface RatingService {

    PaginationResponse fetchAllRatings(Specification<Rating> spec, Pageable pageable);

    PaginationResponse fetchListRatingsByBookId(Long bookId, Pageable pageable);

    Rating createRating(Rating ratingRequest);

    Rating getRatingById(Long id);

    Rating getRatingByUserIdAndBookId(Long userId, Long bookId);

    boolean checkIdExists(Long id);

    void deleteRatingById(Long id);
}
