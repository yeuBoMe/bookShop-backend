package com.fullstack.bookShop.service.interfaces;

import com.fullstack.bookShop.dto.request.BookCreateRequest;
import com.fullstack.bookShop.dto.response.BookResponse;
import com.fullstack.bookShop.dto.response.imports.ImportFileResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.exception.custom.FileException;
import com.fullstack.bookShop.model.Book;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

public interface BookService {

    PaginationResponse fetchAllBooks(Specification<Book> spec, Pageable pageable);

    PaginationResponse fetchListBooksByMinRatingGreaterThanEqual(Double minRating, Pageable pageable);

    List<BookResponse> fetchAllBookResponsesFromBooks();

    Book createBook(Book bookRequest);

    Book updateBookById(Long id, Book bookRequest);

    Book partialUpdateBookById(Long id, Book bookRequest);

    Book uploadBookSingleImageInLocal(Long id, MultipartFile file)
            throws FileException,
                   URISyntaxException,
                   IOException;

    Book uploadBookSingleImageToCloud(Long id, MultipartFile file) throws FileException, IOException;

    Book uploadBookMultipleImagesInLocal(Long id, List<MultipartFile> fileList)
            throws FileException,
                   URISyntaxException,
                   IOException;

    Book uploadBookMultipleImagesToCloud(Long id, List<MultipartFile> fileList) throws FileException, IOException;

    Book removeBookSingleImageInLocal(Long id, String image) throws URISyntaxException, IOException;

    Book removeBookSingleImageInCloud(Long id, String image) throws IOException;

    Book removeBookMultipleImagesInLocal(Long id, List<String> imageList) throws URISyntaxException, IOException;

    Book removeBookMultipleImagesInCloud(Long id, List<String> imageList) throws Exception;

    Book getBookById(Long id);

    void deleteBookById(Long id);

    boolean checkIdExists(Long id);

    boolean checkNameAndAuthorExistsIgnoreCase(String name, String author);

    Set<String> findExistingNamesAndAuthorIgnoreCase(Set<String> keys);

    BookResponse getBookResponseById(Long id);

    ImportFileResponse<BookResponse> importBooksFromFile(List<BookCreateRequest> bookCreateRequestList);
}
