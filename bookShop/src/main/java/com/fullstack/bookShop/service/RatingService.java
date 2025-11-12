package com.fullstack.bookShop.service;

import com.fullstack.bookShop.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.dto.rating.RestRatingCreateDTO;
import com.fullstack.bookShop.dto.rating.RestRatingViewDTO;
import com.fullstack.bookShop.model.Rating;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface RatingService {
    Rating createRating(Rating rating);
    Rating getRatingById(Long id);
    Rating getRatingByUserIdAndBookId(Long userId, Long bookId);

    ResultPaginationDTO fetchListRatingsByBookId(Long bookId, Pageable pageable);
    ResultPaginationDTO fetchListRatings(Specification<Rating> spec, Pageable pageable);

    boolean checkIdExists(Long id);
    void deleteRatingById(Long id);

    RestRatingCreateDTO convertRatingToRestRatingCreateDTO(Rating rating);
    RestRatingViewDTO convertRatingToRestRatingViewDTO(Rating rating);
}
