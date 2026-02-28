package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.dto.response.RatingResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.mapper.RatingMapper;
import com.fullstack.bookShop.model.Rating;
import com.fullstack.bookShop.service.interfaces.RatingService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.annotation.IsAdmin;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingMapper ratingMapper;

    private final RatingService ratingService;

    @IsAdmin
    @GetMapping
    @ApiMessage(ActionConstant.RATING_FETCH_ALL)
    public ResponseEntity<PaginationResponse> fetchAllRatingsRequest(
            @Filter Specification<Rating> spec,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ratingService.fetchAllRatings(spec, pageable));
    }

    @GetMapping("/book/{bookId}")
    @ApiMessage(ActionConstant.RATING_FETCH_BY_BOOK)
    public ResponseEntity<PaginationResponse> fetchListRatingsByBookId(
            @PathVariable Long bookId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ratingService.fetchListRatingsByBookId(bookId, pageable));
    }

    @PostMapping
    @ApiMessage(ActionConstant.RATING_CREATE)
    public ResponseEntity<RatingResponse> createRatingRequest(@Valid @RequestBody Rating ratingRequest) {
        Rating ratingCreated = ratingService.createRating(ratingRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ratingMapper.toResponse(ratingCreated, ActionConstant.CREATE));
    }

    @GetMapping("/book/{bookId}/user/{userId}")
    @ApiMessage(ActionConstant.RATING_GET_BY_USER_AND_BOOK)
    public ResponseEntity<RatingResponse> fetchRatingsByUserIdAndBookId(
            @PathVariable Long bookId,
            @PathVariable Long userId
    ) {
        Rating ratingGetByUserAndBook = ratingService.getRatingByUserIdAndBookId(userId, bookId);
        return ResponseEntity.ok(ratingMapper.toResponse(ratingGetByUserAndBook, ActionConstant.FETCH));
    }

    @IsAdmin
    @GetMapping("/{id}")
    @ApiMessage(ActionConstant.RATING_GET)
    public ResponseEntity<RatingResponse> getRatingByIdRequest(@PathVariable Long id) {
        Rating ratingGetById = ratingService.getRatingById(id);
        return ResponseEntity.ok(ratingMapper.toResponse(ratingGetById, ActionConstant.FETCH));
    }

    @IsAdmin
    @DeleteMapping("/{id}")
    @ApiMessage(ActionConstant.RATING_DELETE)
    public ResponseEntity<Void> deleteRatingByIdRequest(@PathVariable Long id) {
        ratingService.deleteRatingById(id);
        return ResponseEntity.noContent().build();
    }
}
