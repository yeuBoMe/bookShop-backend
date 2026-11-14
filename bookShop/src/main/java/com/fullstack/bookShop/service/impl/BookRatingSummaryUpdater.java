package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.response.BookRatingSummaryResponse;
import com.fullstack.bookShop.repository.BookRepository;
import com.fullstack.bookShop.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Tinh lai average_stars / rating_count tren books moi khi tap danh gia doi.
 *
 * <p>Gom ve mot cho vi co BON duong lam doi danh gia: them danh gia, xoa danh gia,
 * xoa nguoi dung (keo theo danh gia cua ho), va import nguoi dung voi hanh dong DELETE.
 * Thieu bat ky duong nao la cot diem tren books sai vinh vien, vi khong con cho nao tinh lai.
 */
@Component
@RequiredArgsConstructor
public class BookRatingSummaryUpdater {

    private final BookRepository bookRepository;

    private final RatingRepository ratingRepository;

    /**
     * Sach vua bi go het danh gia van phai duoc dua ve 0, nen luon doc theo danh sach id truyen vao.
     */
    public void refresh(Collection<Long> bookIds) {
        Set<Long> targetIds = bookIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (targetIds.isEmpty()) {
            return;
        }

        Map<Long, BookRatingSummaryResponse> summaryByBookId =
                ratingRepository.getRatingSummariesByBookIds(targetIds)
                        .stream()
                        .collect(Collectors.toMap(
                                BookRatingSummaryResponse::getBookId,
                                Function.identity(),
                                (first, second) -> first));

        for (Long bookId : targetIds) {
            BookRatingSummaryResponse summary = summaryByBookId.get(bookId);
            bookRepository.updateRatingSummary(
                    bookId,
                    summary != null ? roundToOneDecimal(summary.getAverageStars()) : 0.0,
                    summary != null ? summary.getRatingCount().intValue() : 0
            );
        }
    }

    public void refreshRatedBooksOfUser(Long userId) {
        refresh(new HashSet<>(ratingRepository.findRatedBookIdsByUserId(userId)));
    }

    /**
     * Cot nay chi de hien thi va sap xep, giu 15 chu so thap phan chi lam ban API, CSV va man hinh.
     */
    private double roundToOneDecimal(Double value) {
        return value != null ? Math.round(value * 10.0) / 10.0 : 0.0;
    }
}
