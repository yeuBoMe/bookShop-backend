package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.response.BookResponse;
import com.fullstack.bookShop.dto.response.imports.ImportFileResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.mapper.BookMapper;
import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.repository.BookRepository;
import com.fullstack.bookShop.repository.OrderDetailRepository;
import com.fullstack.bookShop.repository.RatingRepository;
import com.fullstack.bookShop.service.interfaces.BookService;
import com.fullstack.bookShop.service.interfaces.CategoryService;
import com.fullstack.bookShop.util.constant.CommonMessage;
import com.fullstack.bookShop.util.enums.BookEnum;
import com.fullstack.bookShop.util.enums.CategoryEnum;
import com.fullstack.bookShop.util.pagination.PaginationUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookServiceImpl.class);

    private final BookMapper bookMapper;

    private final BookRepository bookRepository;

    private final CategoryService categoryService;

    private final RatingRepository ratingRepository;

    private final OrderDetailRepository orderDetailRepository;

    @Override
    public PaginationResponse fetchAllBooks(Specification<Book> spec, Pageable pageable) {
        Page<Book> bookPage = bookRepository.findAll(spec, pageable);

        List<BookResponse> bookResponseList = bookPage.getContent()
                .stream()
                .map(bookMapper::convertBookToViewResponse)
                .toList();

        PaginationResponse paginationResponse = PaginationUtil.fetchAllDataWithPagination(bookPage, pageable);
        paginationResponse.setResult(bookResponseList);
        return paginationResponse;
    }

    @Override
    public PaginationResponse fetchListBooksByMinRatingGreaterThanEqual(Double minRating, Pageable pageable) {
        Page<Book> bookPageWithAverageRating = bookRepository.findAllByMinRatingGreaterThanEqual(minRating, pageable);

        List<BookResponse> bookResponseList = bookPageWithAverageRating.getContent()
                .stream()
                .map(bookMapper::convertBookToViewResponse)
                .toList();

        PaginationResponse paginationResponse =
                PaginationUtil.fetchAllDataWithPagination(bookPageWithAverageRating, pageable);
        paginationResponse.setResult(bookResponseList);
        return paginationResponse;
    }

    @Override
    public List<BookResponse> fetchAllBookResponsesFromBooks() {
        return bookRepository.findAll()
                .stream()
                .map(bookMapper::convertBookToViewResponse)
                .toList();
    }

    @Transactional
    @Override
    public Book createBook(Book bookRequest) {
        LOGGER.info(
                "Creating book name={}, introduceText={}, description={}, " +
                "reader={}, price={}, quantity={}, author={}, sold={}, category={}",
                bookRequest.getName(),
                bookRequest.getIntroduceText(),
                bookRequest.getDescription(),
                bookRequest.getReader(),
                bookRequest.getPrice(),
                bookRequest.getQuantity(),
                bookRequest.getAuthor(),
                bookRequest.getSold(),
                Optional.ofNullable(bookRequest.getCategory())
                        .map(Category::getName)
                        .orElse(null)
        );

        validateAndSetCategory(bookRequest, bookRequest);
        validateBookForCreate(bookRequest);

        Book bookCreated = bookRepository.save(bookRequest);
        LOGGER.info("Create book successfully with id: {}", bookCreated.getId());

        return bookCreated;
    }

    @Transactional
    @Override
    public Book updateBookById(Long id, Book bookRequest) {
        LOGGER.info("Updating book id: {}", id);
        Book bookGetById = getBookById(id);

        validateAndSetCategory(bookGetById, bookRequest);
        validateBookForUpdate(bookGetById, bookRequest);

        bookGetById.setName(bookRequest.getName());
        bookGetById.setPrice(bookRequest.getPrice());
        bookGetById.setQuantity(bookRequest.getQuantity());
        bookGetById.setAuthor(bookRequest.getAuthor());
        bookGetById.setDescription(bookRequest.getDescription());
        bookGetById.setIntroduceText(bookRequest.getIntroduceText());
        bookGetById.setReader(bookRequest.getReader());

        LOGGER.info("Update book successfully with id: {}", id);
        return bookRepository.save(bookGetById);
    }

    @Transactional
    @Override
    public Book partialUpdateBookById(Long id, Book bookUpdated) {
        LOGGER.info("Updating book partial id: {}", id);
        Book bookGetById = getBookById(id);

        validateAndSetCategory(bookGetById, bookUpdated);

        if (bookUpdated.getName() != null && !bookUpdated.getName().trim().isEmpty()) {
            validateBookForUpdate(bookGetById, bookUpdated);
            bookGetById.setName(bookUpdated.getName());
        }
        if (bookUpdated.getPrice() != null) {
            bookGetById.setPrice(bookUpdated.getPrice());
        }
        if (bookUpdated.getQuantity() != null) {
            bookGetById.setQuantity(bookUpdated.getQuantity());
        }
        if (bookUpdated.getAuthor() != null && !bookUpdated.getAuthor().trim().isEmpty()) {
            validateBookForUpdate(bookGetById, bookUpdated);
            bookGetById.setAuthor(bookUpdated.getAuthor());
        }
        if (bookUpdated.getDescription() != null && !bookUpdated.getDescription().trim().isEmpty()) {
            bookGetById.setDescription(bookUpdated.getDescription());
        }
        if (bookUpdated.getIntroduceText() != null && !bookUpdated.getIntroduceText().trim().isEmpty()) {
            bookGetById.setIntroduceText(bookUpdated.getIntroduceText());
        }
        if (bookUpdated.getReader() != null && !bookUpdated.getReader().trim().isEmpty()) {
            bookGetById.setReader(bookUpdated.getReader());
        }

        LOGGER.info("Update book partial successfully with id: {}", id);
        return bookRepository.save(bookGetById);
    }

    @Override
    public Book uploadBookSingleImage(Long id, String image) {
        LOGGER.info("Uploading image for book id={}, image={}", id, image);

        Book bookGetById = getBookById(id);
        bookGetById.setImages(List.of(image));

        LOGGER.info("Upload image successfully for book id: {}", id);
        return bookRepository.save(bookGetById);
    }

    @Override
    public Book uploadBookMultipleImages(Long id, List<String> imageList) {
        LOGGER.info("Uploading multiple images for book id={}, size={}", id, imageList.size());

        Book bookGetById = getBookById(id);
        bookGetById.setImages(imageList);

        LOGGER.info("Upload multiple images successfully for book id: {}", id);
        return bookRepository.save(bookGetById);
    }

    @Override
    public Book removeBookSingleImage(Long id, String image) {
        LOGGER.info("Removing single image for book id={}, image={}", id, image);

        Book bookGetById = getBookById(id);
        bookGetById.getImages().remove(image);

        LOGGER.info("Remove image successfully for book id: {}", id);
        return bookRepository.save(bookGetById);
    }

    @Override
    public Book removeBookMultipleImages(Long id, List<String> imageList) {
        LOGGER.info("Removing multiple images for book id={}, size={}", id, imageList.size());

        Book bookGetById = getBookById(id);
        bookGetById.getImages().removeAll(imageList);

        LOGGER.info("Remove multiple images successfully for book id: {}", id);
        return bookRepository.save(bookGetById);
    }

    @Override
    public Book getBookById(Long id) {
        return bookRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        String.format(BookEnum.ID_NOT_FOUND.getMessage(), id)
                ));
    }

    @Transactional
    @Override
    public void deleteBookById(Long id) {
        LOGGER.info("Deleting book id: {}", id);
        if (!checkIdExists(id)) {
            throw new NoSuchElementException(
                    String.format(BookEnum.ID_NOT_FOUND.getMessage(), id)
            );
        }
        ratingRepository.deleteByBookId(id);
        orderDetailRepository.deleteByBookId(id);
        bookRepository.deleteById(id);
        LOGGER.info("Delete book successfully with id: {}", id);
    }

    @Override
    public boolean checkIdExists(Long id) {
        return bookRepository.existsById(id);
    }

    @Override
    public boolean checkNameAndAuthorExistsIgnoreCase(String name, String author) {
        return bookRepository.existsByNameAndAuthorIgnoreCase(name, author);
    }

    @Override
    public ImportFileResponse<BookResponse> importBooksFromFile(List<Book> bookList) {
        ImportFileResponse<BookResponse> importFileResponse = new ImportFileResponse<>();
        List<BookResponse> successData = new ArrayList<>();
        List<ImportFileResponse.DetailFaultInfo<BookResponse>> detailFaultInfoList = new ArrayList<>();

        int countSuccess = 0;
        int countError = 0;

        for (int i = 0; i < bookList.size(); i++) {
            Book book = bookList.get(i);
            BookResponse bookResponse = bookMapper.convertBookToCreateResponse(book);

            try {
                validateAndSetCategory(book, book);
                validateBookForCreate(book);
                bookRepository.save(book);

                successData.add(bookResponse);
                countSuccess++;

            } catch (ConstraintViolationException e) {
                List<String> errorMessage = e.getConstraintViolations()
                        .stream()
                        .map(ConstraintViolation::getMessage)
                        .toList();

                detailFaultInfoList.add(new ImportFileResponse.DetailFaultInfo<>(
                        i + 1,
                        errorMessage,
                        bookResponse
                ));

                countError++;

            } catch (Exception e) {
                detailFaultInfoList.add(new ImportFileResponse.DetailFaultInfo<>(
                        i + 1,
                        e.getMessage(),
                        bookResponse
                ));

                countError++;
            }
        }

        importFileResponse.setCountSuccess(countSuccess);
        importFileResponse.setCountError(countError);
        importFileResponse.setDetail(countError > 0 ? detailFaultInfoList : CommonMessage.IMPORT_SUCCESS);
        importFileResponse.setItems(successData);

        return importFileResponse;
    }

    private void validateBookForCreate(Book bookRequest) {
        if (checkNameAndAuthorExistsIgnoreCase(bookRequest.getName(), bookRequest.getAuthor())) {
            throw new IllegalArgumentException(
                    String.format(
                            BookEnum.NAME_AUTHOR_EXISTS.getMessage(),
                            bookRequest.getName(),
                            bookRequest.getAuthor()
                    )
            );
        }
    }

    private void validateBookForUpdate(Book currentBook, Book bookRequest) {
        boolean isNameAndAuthorExists = checkNameAndAuthorExistsIgnoreCase(
                bookRequest.getName(),
                bookRequest.getAuthor()
        );

        if (isNameAndAuthorExists && !(
                currentBook.getName().equals(bookRequest.getName())
                        && currentBook.getAuthor().equals(bookRequest.getAuthor())
        )) {
            throw new IllegalArgumentException(
                    String.format(
                            BookEnum.NAME_AUTHOR_EXISTS.getMessage(),
                            bookRequest.getName(),
                            bookRequest.getAuthor()
                    )
            );
        }
    }

    private void validateAndSetCategory(Book currentBook, Book bookRequest) {
        if (bookRequest.getCategory() != null) {
            Long categoryId = bookRequest.getCategory().getId();
            if (!categoryService.checkIdExists(categoryId)) {
                throw new NoSuchElementException(
                        String.format(CategoryEnum.ID_NOT_FOUND.getMessage(), categoryId)
                );
            }
            Category categoryGetById = categoryService.getCategoryById(categoryId);
            currentBook.setCategory(categoryGetById);
        }
    }
}
