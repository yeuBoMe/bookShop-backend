package com.fullstack.bookShop.service.impl.imports;

import com.fullstack.bookShop.dto.request.imports.BookImportRequest;
import com.fullstack.bookShop.dto.response.BookResponse;
import com.fullstack.bookShop.mapper.BookMapper;
import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.repository.BookRepository;
import com.fullstack.bookShop.repository.CategoryRepository;
import com.fullstack.bookShop.repository.OrderDetailRepository;
import com.fullstack.bookShop.repository.RatingRepository;
import com.fullstack.bookShop.service.impl.imports.BookImportServiceImpl.BookImportContext;
import com.fullstack.bookShop.util.batch.ImportExecutor;
import com.fullstack.bookShop.util.batch.ImportRow;
import com.fullstack.bookShop.util.constant.SymbolConstant;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import com.fullstack.bookShop.util.enums.BookEnum;
import com.fullstack.bookShop.util.enums.CategoryEnum;
import com.fullstack.bookShop.util.enums.ImportActionEnum;
import com.fullstack.bookShop.util.message.MessageTranslator;
import com.fullstack.bookShop.util.string.StringUtils;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BookImportServiceImpl
        extends AbstractEntityImportService<BookImportRequest, Book, BookResponse, BookImportContext> {

    private final BookMapper bookMapper;

    private final BookRepository bookRepository;

    private final CategoryRepository categoryRepository;

    private final RatingRepository ratingRepository;

    private final OrderDetailRepository orderDetailRepository;

    public BookImportServiceImpl(
            Validator validator,
            ImportExecutor importExecutor,
            MessageTranslator messageTranslator,
            BookMapper bookMapper,
            BookRepository bookRepository,
            CategoryRepository categoryRepository,
            RatingRepository ratingRepository,
            OrderDetailRepository orderDetailRepository
    ) {
        super(validator, importExecutor, messageTranslator);
        this.bookMapper = bookMapper;
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.ratingRepository = ratingRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    /**
     * Du lieu tra cuu cua ca file, nap bang DUNG hai cau query.
     * De moi dong tu tra DB thi 5.000 dong sinh them 10.000 cau khong can thiet.
     */
    protected record BookImportContext(Set<String> existingKeys, Map<String, Category> categoriesByName) {
    }

    @Override
    protected BookImportContext buildContext(List<ImportRow<BookImportRequest>> rows) {
        Set<String> keys = rows.stream()
                .map(row -> businessKeyOf(row.source()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> categoryNames = rows.stream()
                .map(row -> StringUtils.trimLowerBlank(row.source().getCategory()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> existingKeys = keys.isEmpty()
                ? Set.of()
                : bookRepository.findExistingNamesAndAuthorsIgnoreCase(keys);

        Map<String, Category> categoriesByName = categoryNames.isEmpty()
                ? Map.of()
                : categoryRepository.findAllByNamesIgnoreCase(categoryNames)
                        .stream()
                        .collect(Collectors.toMap(
                                category -> StringUtils.trimLowerBlank(category.getName()),
                                Function.identity(),
                                (first, second) -> first));

        return new BookImportContext(existingKeys, categoriesByName);
    }

    @Override
    protected String businessKeyOf(BookImportRequest request) {
        String name = StringUtils.trimLowerBlank(request.getName());
        String author = StringUtils.trimLowerBlank(request.getAuthor());
        return name != null && author != null ? name + SymbolConstant.UNDERSCORE + author : null;
    }

    @Override
    protected ImportActionEnum actionOf(BookImportRequest request) {
        return request.getAction();
    }

    @Override
    protected Optional<Book> findExistingEntity(BookImportRequest request) {
        return bookRepository.findByNameAndAuthorIgnoreCase(request.getName(), request.getAuthor());
    }

    @Override
    protected String notFoundMessage(BookImportRequest request) {
        return messageTranslator.toLocale(
                BookEnum.NAME_AUTHOR_NOT_FOUND.getMessageKey(),
                request.getName(),
                request.getAuthor()
        );
    }

    @Override
    protected Book createEntity(BookImportRequest request, BookImportContext context) {
        if (context.existingKeys().contains(businessKeyOf(request))) {
            throw new IllegalArgumentException(messageTranslator.toLocale(
                    BookEnum.NAME_AUTHOR_EXISTS.getMessageKey(), request.getName(), request.getAuthor()));
        }

        Book book = new Book();
        applyFields(book, request, context);
        return bookRepository.save(book);
    }

    @Override
    protected Book updateEntity(Book existing, BookImportRequest request, BookImportContext context) {
        applyFields(existing, request, context);
        return bookRepository.save(existing);
    }

    @Override
    protected void deleteEntity(Book existing) {
        // Giu nguyen luat cua luong xoa sach: don hang la lich su, khong duoc mat dong hang
        if (orderDetailRepository.existsByBookId(existing.getId())) {
            throw new IllegalStateException(messageTranslator.toLocale(
                    BookEnum.REFERENCED_BY_ORDER.getMessageKey(), existing.getId()));
        }

        ratingRepository.deleteByBookId(existing.getId());
        bookRepository.delete(existing);
    }

    @Override
    protected BookResponse toResponse(Book entity, ImportActionEnum action) {
        return bookMapper.toResponse(entity, switch (action) {
            case CREATE -> ActionConstant.CREATE;
            case UPDATE -> ActionConstant.UPDATE;
            case DELETE -> ActionConstant.DELETE;
        });
    }

    private void applyFields(Book book, BookImportRequest request, BookImportContext context) {
        book.setName(StringUtils.normalizeSpaces(request.getName()));
        book.setAuthor(StringUtils.normalizeSpaces(request.getAuthor()));
        book.setIntroduceText(request.getIntroduceText());
        book.setDescription(request.getDescription());
        book.setReader(request.getReader());
        book.setPrice(request.getPrice());
        book.setQuantity(request.getQuantity());
        book.setSold(request.getSold());
        book.setCategory(resolveCategory(request.getCategory(), context));
    }

    private Category resolveCategory(String categoryName, BookImportContext context) {
        String normalized = StringUtils.trimLowerBlank(categoryName);
        if (normalized == null) {
            return null;
        }

        Category category = context.categoriesByName().get(normalized);
        if (category == null) {
            throw new IllegalArgumentException(messageTranslator.toLocale(
                    CategoryEnum.NAME_NOT_FOUND.getMessageKey(), categoryName));
        }
        return category;
    }
}
