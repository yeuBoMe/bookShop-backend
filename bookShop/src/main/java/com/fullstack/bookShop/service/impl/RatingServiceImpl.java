package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.response.RatingResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.mapper.RatingMapper;
import com.fullstack.bookShop.model.Rating;
import com.fullstack.bookShop.repository.RatingRepository;
import com.fullstack.bookShop.service.interfaces.BookService;
import com.fullstack.bookShop.service.interfaces.RatingService;
import com.fullstack.bookShop.service.interfaces.UserService;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import com.fullstack.bookShop.util.constant.CommonMessage;
import com.fullstack.bookShop.util.enums.BookEnum;
import com.fullstack.bookShop.util.enums.RatingEnum;
import com.fullstack.bookShop.util.security.SecurityUtil;
import com.fullstack.bookShop.util.pagination.PaginationUtil;
import com.fullstack.bookShop.util.message.MessageTranslator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RatingServiceImpl.class);

    private final MessageTranslator messageTranslator;

    private final UserService userService;

    private final BookService bookService;

    private final RatingMapper ratingMapper;

    private final RatingRepository ratingRepository;

    private final BookRatingSummaryUpdater bookRatingSummaryUpdater;

    @Override
    public PaginationResponse fetchAllRatings(Specification<Rating> spec, Pageable pageable) {
        Page<Rating> ratingPage = ratingRepository.findAll(spec, pageable);

        List<RatingResponse> ratingResponseList = ratingPage.getContent()
                .stream()
                .map(rating -> ratingMapper.toResponse(rating, ActionConstant.FETCH))
                .toList();

        PaginationResponse paginationResponse = PaginationUtil.fetchAllDataWithPagination(ratingPage, pageable);
        paginationResponse.setResult(ratingResponseList);
        return paginationResponse;
    }

    @Override
    public PaginationResponse fetchListRatingsByBookId(Long bookId, Pageable pageable) {
        Page<Rating> ratingPage = ratingRepository.findAllByBookId(bookId, pageable);

        List<RatingResponse> ratingResponseList = ratingPage.getContent()
                .stream()
                .map(rating -> ratingMapper.toResponse(rating, ActionConstant.FETCH))
                .toList();

        PaginationResponse paginationResponse = PaginationUtil.fetchAllDataWithPagination(ratingPage, pageable);
        paginationResponse.setResult(ratingResponseList);
        return paginationResponse;
    }

    @Transactional
    @Override
    public Rating createRating(Rating ratingRequest) {
        LOGGER.info(
                "Creating rating stars={}, comment={}",
                ratingRequest.getStars(),
                ratingRequest.getComment()
        );

        applyAuthenticatedUser(ratingRequest);
        validateAndSetBook(ratingRequest);
        validateRatingNotDuplicated(ratingRequest);

        Rating ratingCreated = ratingRepository.save(ratingRequest);
        bookRatingSummaryUpdater.refresh(List.of(ratingCreated.getBook().getId()));

        LOGGER.info("Create rating successfully with id: {}", ratingCreated.getId());

        return ratingCreated;
    }

    @Override
    public Rating getRatingById(Long id) {
        return ratingRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        messageTranslator.toLocale(RatingEnum.ID_NOT_FOUND.getMessageKey(), id)
                ));
    }

    @Override
    public Rating getRatingByUserIdAndBookId(Long userId, Long bookId) {
        return ratingRepository
                .findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new NoSuchElementException(
                        messageTranslator.toLocale(RatingEnum.USER_BOOK_NOT_FOUND.getMessageKey(), userId, bookId)
                ));
    }

    @Override
    public boolean checkIdExists(Long id) {
        return ratingRepository.existsById(id);
    }

    @Transactional
    @Override
    public void deleteRatingById(Long id) {
        LOGGER.info("Deleting rating id: {}", id);
        if (!checkIdExists(id)) {
            throw new NoSuchElementException(
                    messageTranslator.toLocale(RatingEnum.ID_NOT_FOUND.getMessageKey(), id)
            );
        }
        Rating ratingToDelete = getRatingById(id);
        Long ratedBookId = ratingToDelete.getBook() != null
                ? ratingToDelete.getBook().getId()
                : null;

        ratingRepository.deleteById(id);
        bookRatingSummaryUpdater.refresh(Collections.singletonList(ratedBookId));

        LOGGER.info("Delete rating successfully with id: {}", id);
    }

    /** Chan truoc unique(user_id, book_id): de DB nem thi client nhan 500 kem nguyen cau SQL. */
    private void validateRatingNotDuplicated(Rating ratingRequest) {
        if (ratingRequest.getUser() == null || ratingRequest.getBook() == null) {
            return;
        }

        Long userId = ratingRequest.getUser().getId();
        Long bookId = ratingRequest.getBook().getId();
        if (ratingRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new IllegalArgumentException(
                    messageTranslator.toLocale(RatingEnum.USER_BOOK_ALREADY_RATED.getMessageKey(), userId, bookId)
            );
        }
    }

    /**
     * Nguoi danh gia LUON lay tu token, khong lay tu body.
     *
     * <p>Truoc day body tu khai user.id nen bat ky tai khoan nao cung dat duoc
     * danh gia dung ten nguoi khac - va bang unique(user_id, book_id) khien nan nhan
     * khong the tu danh gia lai cuon sach do nua.
     */
    private void applyAuthenticatedUser(Rating rating) {
        String email = SecurityUtil.getCurrentUserLoginEmail()
                .orElseThrow(() -> new AccessDeniedException(
                        messageTranslator.toLocale(CommonMessage.JWT_FAILED)
                ));

        rating.setUser(userService.getUserByEmail(email));
    }

    private void validateAndSetBook(Rating ratingRequest) {
        if (ratingRequest.getBook() == null) {
            return;
        }

        Long bookId = ratingRequest.getBook().getId();
        if (!bookService.checkIdExists(bookId)) {
            throw new NoSuchElementException(
                    messageTranslator.toLocale(BookEnum.ID_NOT_FOUND.getMessageKey(), bookId)
            );
        }
        ratingRequest.setBook(bookService.getBookById(bookId));
    }
}
