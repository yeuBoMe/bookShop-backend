package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.domain.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.domain.dto.rating.RestRatingCreateDTO;
import com.fullstack.bookShop.domain.dto.rating.RestRatingViewDTO;
import com.fullstack.bookShop.domain.entity.Rating;
import com.fullstack.bookShop.service.RatingService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @ApiMessage("Create rating")
    @PostMapping
    public ResponseEntity<RestRatingCreateDTO> createRatingRequest(@Valid @RequestBody Rating ratingRequest) {
        Rating ratingCreated = this.ratingService.createRating(ratingRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.ratingService.convertRatingToRestRatingCreateDTO(ratingCreated));
    }

    @ApiMessage("Fetch list ratings")
    @GetMapping
    public ResponseEntity<ResultPaginationDTO> fetchAllRatingsRequest(
            @Filter Specification<Rating> spec,
            Pageable pageable
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.ratingService.fetchListRatings(spec, pageable));
    }

    @ApiMessage("Fetch list ratings by book")
    @GetMapping("/book/{bookId}")
    public ResponseEntity<ResultPaginationDTO> fetchAllRatingsByBookId(@PathVariable Long bookId, Pageable pageable) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.ratingService.fetchListRatingsByBookId(bookId, pageable));
    }

    @ApiMessage("Get rating by user and book")
    @GetMapping("/book/{bookId}/user/{userId}")
    public ResponseEntity<RestRatingViewDTO> fetchRatingsByUserIdAndBookId(@PathVariable Long bookId, @PathVariable Long userId) {
        Rating ratingGetByUserIdAndBookId = this.ratingService.getRatingByUserIdAndBookId(userId, bookId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.ratingService.convertRatingToRestRatingViewDTO(ratingGetByUserIdAndBookId));
    }

    @ApiMessage("Get rating")
    @GetMapping("/{id}")
    public ResponseEntity<RestRatingViewDTO> getRatingByIdRequest(@PathVariable Long id) {
        Rating ratingGetById = this.ratingService.getRatingById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.ratingService.convertRatingToRestRatingViewDTO(ratingGetById));
    }

    @ApiMessage("Delete rating")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRatingByIdRequest(@PathVariable Long id) {
        this.ratingService.deleteRatingById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(null);
    }
}
