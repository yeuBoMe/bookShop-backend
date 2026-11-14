package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.request.BookSearchRequest;
import com.fullstack.bookShop.dto.response.BookResponse;
import com.fullstack.bookShop.dto.response.cursor.CursorPageResponse;
import com.fullstack.bookShop.mapper.BookMapper;
import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.repository.BookRepository;
import com.fullstack.bookShop.service.interfaces.BookSearchService;
import com.fullstack.bookShop.specification.BookSpecification;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import com.fullstack.bookShop.util.cursor.CursorUtil;
import com.fullstack.bookShop.util.enums.BookSortEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookSearchServiceImpl implements BookSearchService {

    private final BookMapper bookMapper;

    private final BookRepository bookRepository;

    @Value("${bookshop.search.default-size}")
    private int defaultSize;

    @Value("${bookshop.search.max-size}")
    private int maxSize;

    @Override
    public CursorPageResponse<BookResponse> search(BookSearchRequest request) {
        BookSortEnum sortMode = BookSortEnum.resolveOrDefault(request.getSort());
        int size = resolveSize(request.getSize());
        CursorUtil.Cursor cursor = CursorUtil.decode(request.getCursor());

        Specification<Book> specification = BookSpecification.hasKeyword(request.getKeyword())
                .and(BookSpecification.hasCategoryName(request.getCategoryName()))
                .and(BookSpecification.hasCategoryIn(request.getCategoryIds()))
                .and(BookSpecification.priceFrom(request.getMinPrice()))
                .and(BookSpecification.priceTo(request.getMaxPrice()))
                .and(BookSpecification.hasMinAverageRating(request.getMinRating()))
                .and(BookSpecification.afterCursor(sortMode, cursor));

        // Lay du MOT ban ghi de biet con trang sau hay khong - re hon nhieu so voi dem tong
        List<Book> rows = bookRepository.findBy(
                specification,
                query -> query.sortBy(sortMode.toSort()).limit(size + 1).all()
        );

        boolean hasNext = rows.size() > size;
        List<Book> pageRows = hasNext ? rows.subList(0, size) : rows;

        List<BookResponse> items = pageRows.stream()
                .map(book -> bookMapper.toResponse(book, ActionConstant.FETCH))
                .toList();

        String nextCursor = hasNext ? encodeCursor(pageRows.getLast(), sortMode) : null;
        return CursorPageResponse.of(items, nextCursor);
    }

    /** Chan client tu nang size len vai nghin roi keo sap DB. */
    private int resolveSize(Integer requestedSize) {
        if (requestedSize == null || requestedSize <= 0) {
            return defaultSize;
        }
        return Math.min(requestedSize, maxSize);
    }

    private String encodeCursor(Book lastBook, BookSortEnum sortMode) {
        Object sortValue = switch (sortMode) {
            case NEWEST -> lastBook.getCreatedAt();
            case PRICE_ASC, PRICE_DESC -> lastBook.getPrice();
            case RATING_DESC -> lastBook.getAverageStars();
        };

        return CursorUtil.encode(sortValue, lastBook.getId());
    }
}
