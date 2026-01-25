package com.fullstack.bookShop.service.interfaces;

import com.fullstack.bookShop.dto.response.BookResponse;
import com.fullstack.bookShop.dto.response.imports.ImportFileResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.model.Book;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface BookService {

    PaginationResponse fetchAllBooks(Specification<Book> spec, Pageable pageable);

    PaginationResponse fetchListBooksByMinRatingGreaterThanEqual(Double minRating, Pageable pageable);

    List<BookResponse> fetchAllBookResponsesFromBooks();

    Book createBook(Book bookRequest);

    Book updateBookById(Long id, Book bookRequest);

    Book partialUpdateBookById(Long id, Book bookRequest);

    Book uploadBookSingleImage(Long id, String image);

    Book uploadBookMultipleImages(Long id, List<String> imageList);

    Book removeBookSingleImage(Long id, String image);

    Book removeBookMultipleImages(Long id, List<String> imageList);

    Book getBookById(Long id);

    void deleteBookById(Long id);

    boolean checkIdExists(Long id);

    boolean checkNameAndAuthorExistsIgnoreCase(String name, String author);

    ImportFileResponse<BookResponse> importBooksFromFile(List<Book> bookList);
}
