package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.domain.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.domain.dto.rating.RestRatingCreateDTO;
import com.fullstack.bookShop.domain.dto.rating.RestRatingViewDTO;
import com.fullstack.bookShop.domain.entity.Book;
import com.fullstack.bookShop.domain.entity.Rating;
import com.fullstack.bookShop.domain.entity.User;
import com.fullstack.bookShop.repository.RatingRepository;
import com.fullstack.bookShop.service.BookService;
import com.fullstack.bookShop.service.RatingService;
import com.fullstack.bookShop.service.UserService;
import com.fullstack.bookShop.util.pagination.PageUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;

    private final UserService userService;

    private final BookService bookService;

    public RatingServiceImpl(
            RatingRepository ratingRepository,
            UserService userService,
            BookService bookService
    ) {
        this.ratingRepository = ratingRepository;
        this.userService = userService;
        this.bookService = bookService;
    }

    private void validateAndSetUserAndBook(Rating ratingRequest, Rating currentRating) {
        if (ratingRequest.getUser() != null) {
            if (!this.userService.checkIdExists(ratingRequest.getUser().getId())) {
                throw new NoSuchElementException("User with id '" + ratingRequest.getUser().getId() + "' not exist!");
            }
            User userGetById = this.userService.getUserById(ratingRequest.getUser().getId());
            currentRating.setUser(userGetById);
        }

        if (ratingRequest.getBook() != null) {
            if (!this.bookService.checkIdExists(ratingRequest.getBook().getId())) {
                throw new NoSuchElementException("Book with id '" + ratingRequest.getBook().getId() + "' not exist!");
            }
            Book bookGetById = this.bookService.getBookById(ratingRequest.getBook().getId());
            currentRating.setBook(bookGetById);
        }
    }

    @Transactional
    @Override
    public Rating createRating(Rating rating) {
        this.validateAndSetUserAndBook(rating, rating);
        return this.ratingRepository.save(rating);
    }

    @Override
    public Rating getRatingById(Long id) {
        return this.ratingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Rating with id '" + id + "' not found!"));
    }

    @Override
    public Rating getRatingByUserIdAndBookId(Long userId, Long bookId) {
        return this.ratingRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new NoSuchElementException("No rating found with user id '" + userId + "' and book id '" + bookId + "'!"));
    }

    @Override
    public ResultPaginationDTO fetchListRatingsByBookId(Long bookId, Pageable pageable) {
        Page<Rating> ratingPage = this.ratingRepository.findAllByBookId(bookId, pageable);

        List<RestRatingViewDTO> ratingViewDTOList = ratingPage.getContent().stream()
                .map(this::convertRatingToRestRatingViewDTO)
                .toList();

        ResultPaginationDTO resultPaginationDTO = PageUtil.handleFetchAllDataWithPagination(ratingPage, pageable);
        resultPaginationDTO.setResult(ratingViewDTOList);

        return resultPaginationDTO;
    }

    @Override
    public ResultPaginationDTO fetchListRatings(Specification<Rating> spec, Pageable pageable) {
        Page<Rating> ratingPage = this.ratingRepository.findAll(spec, pageable);

        List<RestRatingViewDTO> ratingViewDTOList = ratingPage.getContent().stream()
                .map(this::convertRatingToRestRatingViewDTO)
                .toList();

        ResultPaginationDTO resultPaginationDTO = PageUtil.handleFetchAllDataWithPagination(ratingPage, pageable);
        resultPaginationDTO.setResult(ratingViewDTOList);

        return resultPaginationDTO;
    }

    @Override
    public boolean checkIdExists(Long id) {
        return this.ratingRepository.existsById(id);
    }

    @Transactional
    @Override
    public void deleteRatingById(Long id) {
        if (!this.checkIdExists(id)) {
            throw new NoSuchElementException("Rating with id '" + id + "' not found!");
        }
        this.ratingRepository.deleteById(id);
    }

    @Override
    public RestRatingCreateDTO convertRatingToRestRatingCreateDTO(Rating rating) {
        RestRatingCreateDTO.UserRatingCreateDTO userRatingCreateDTO = Optional.ofNullable(rating.getUser())
                .map(user -> new RestRatingCreateDTO.UserRatingCreateDTO(
                        user.getId(),
                        user.getFullName(),
                        user.getAvatar()
                ))
                .orElse(null);

        String bookName = Optional.ofNullable(rating.getBook())
                .map(Book::getName)
                .orElse(null);

        return new RestRatingCreateDTO(
                rating.getId(),
                rating.getStars(),
                rating.getComment(),
                userRatingCreateDTO,
                bookName,
                rating.getCreatedAt(),
                rating.getCreatedBy()
        );
    }

    @Override
    public RestRatingViewDTO convertRatingToRestRatingViewDTO(Rating rating) {
        RestRatingViewDTO.UserRatingViewDTO userRatingViewDTO = Optional.ofNullable(rating.getUser())
                .map(user -> new RestRatingViewDTO.UserRatingViewDTO(
                        user.getId(),
                        user.getFullName(),
                        user.getAvatar()
                ))
                .orElse(null);

        String bookName = Optional.ofNullable(rating.getBook())
                .map(Book::getName)
                .orElse(null);

        return new RestRatingViewDTO(
                rating.getId(),
                rating.getStars(),
                rating.getComment(),
                userRatingViewDTO,
                bookName,
                rating.getCreatedAt(),
                rating.getUpdatedAt(),
                rating.getCreatedBy(),
                rating.getUpdatedBy()
        );
    }
}
