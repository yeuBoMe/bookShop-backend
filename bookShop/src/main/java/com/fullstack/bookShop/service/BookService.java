package com.fullstack.bookShop.service;

import com.fullstack.bookShop.dto.book.RestBookCreateDTO;
import com.fullstack.bookShop.dto.book.RestBookUpdateDTO;
import com.fullstack.bookShop.dto.book.RestBookViewDTO;
import com.fullstack.bookShop.dto.imports.RestImportDTO;
import com.fullstack.bookShop.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.model.Book;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface BookService {
    Book createBook(Book book);
    Book updateBookById(Long id, Book book);
    Book updateBookPartialById(Long id, Book book);
    Book getBookById(Long id);

    void deleteBookById(Long id);

    boolean checkIdExists(Long id);
    boolean checkNameAndAuthorExistsIgnoreCase(String name, String author);

    RestBookCreateDTO convertBookToRestBookCreateDTO(Book book);
    RestBookUpdateDTO convertBookToRestBookUpdateDTO(Book book);
    RestBookViewDTO convertBookToRestBookViewDTO(Book book);

    ResultPaginationDTO fetchListBooks(Specification<Book> spec, Pageable pageable);
    ResultPaginationDTO fetchListBooksByMinRatingGreaterThanEqual(Double minRating, Pageable pageable);

    RestImportDTO<RestBookCreateDTO> importListBooksFromFile(List<Book> bookList);
}
