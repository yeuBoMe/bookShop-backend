package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.domain.dto.book.RestBookCreateDTO;
import com.fullstack.bookShop.domain.dto.book.RestBookUpdateDTO;
import com.fullstack.bookShop.domain.dto.book.RestBookViewDTO;
import com.fullstack.bookShop.domain.dto.imports.RestImportDTO;
import com.fullstack.bookShop.domain.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.domain.entity.Book;
import com.fullstack.bookShop.service.BookService;
import com.fullstack.bookShop.service.FileService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.exception.custom.StorageException;
import com.fullstack.bookShop.util.imports.FileValidUtil;
import com.fullstack.bookShop.util.imports.HttpStatusUtil;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    @Value("${bookshop.upload-file.base-uri}")
    private String baseUri;

    private final BookService bookService;

    private final FileService fileService;

    @ApiMessage("Create book")
    @PostMapping
    public ResponseEntity<RestBookCreateDTO> createBookRequest(@Valid @RequestBody Book book) {
        Book bookCreated = bookService.createBook(book);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookService.convertBookToRestBookCreateDTO(bookCreated));
    }

    @ApiMessage("Fetch list books")
    @GetMapping
    public ResponseEntity<ResultPaginationDTO> fetchListBooksRequest(
            @Filter Specification<Book> spec,
            Pageable pageable
    ) {
        return ResponseEntity.ok(bookService.fetchListBooks(spec, pageable));
    }

    @ApiMessage("Fetch list books by average rating")
    @GetMapping("/average-rating")
    public ResponseEntity<ResultPaginationDTO> fetchListBooksByMinRatingGreaterThaneEqualRequest(
            @RequestParam(defaultValue = "0") Double minRating,
            Pageable pageable
    ) {
        return ResponseEntity.ok(bookService.fetchListBooksByMinRatingGreaterThanEqual(minRating, pageable));
    }

    @ApiMessage("Get book")
    @GetMapping("/{id}")
    public ResponseEntity<RestBookViewDTO> getBookByIdRequest(@PathVariable Long id) {
        Book bookGetById = bookService.getBookById(id);
        return ResponseEntity.ok(bookService.convertBookToRestBookViewDTO(bookGetById));
    }

    @ApiMessage("Update book")
    @PutMapping("/{id}")
    public ResponseEntity<RestBookUpdateDTO> updateBookByIdRequest(
            @Valid @RequestBody Book book,
            @PathVariable Long id
    ) {
        Book bookUpdated = bookService.updateBookById(id, book);
        return ResponseEntity.ok(bookService.convertBookToRestBookUpdateDTO(bookUpdated));
    }

    @ApiMessage("Update book partial")
    @PatchMapping("/{id}")
    public ResponseEntity<RestBookUpdateDTO> partialUpdateBookByIdRequest(
            @Valid @RequestBody Book book,
            @PathVariable Long id
    ) {
        Book bookUpdated = bookService.updateBookPartialById(id, book);
        return ResponseEntity.ok(bookService.convertBookToRestBookUpdateDTO(bookUpdated));
    }

    @ApiMessage("Delete book")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookByIdRequest(@PathVariable Long id) {
        bookService.deleteBookById(id);
        return ResponseEntity.noContent().build();
    }

    @ApiMessage("Update book image")
    @PatchMapping("/{id}/image")
    public ResponseEntity<RestBookUpdateDTO> updateBookSingleImageRequest(
            @PathVariable Long id,
            @RequestParam(name = "file", required = false) MultipartFile file
    ) throws
            StorageException,
            URISyntaxException,
            IOException {
        FileValidUtil.validateFile(file);
        fileService.createUploadFolder(baseUri + "book");
        String fileUploaded = fileService.storeSingleFile(file, "book");

        Book book = new Book();
        book.setImages(List.of(fileUploaded));
        Book bookUpdated = bookService.updateBookPartialById(id, book);

        return ResponseEntity.ok(bookService.convertBookToRestBookUpdateDTO(bookUpdated));
    }

    @ApiMessage("Update book multiple images")
    @PatchMapping("/{id}/multiple-images")
    public ResponseEntity<RestBookUpdateDTO> updateBookMultipleImagesRequest(
            @PathVariable Long id,
            @RequestParam(name = "files", required = false) List<MultipartFile> files
    ) throws
            StorageException,
            URISyntaxException,
            IOException {

        FileValidUtil.validateListFiles(files);
        fileService.createUploadFolder(baseUri + "book");
        List<String> fileNames = fileService.storeMultipleFiles(files, "book");

        Book book = new Book();
        book.setImages(fileNames);
        Book bookUpdated = bookService.updateBookPartialById(id, book);

        return ResponseEntity.ok(bookService.convertBookToRestBookUpdateDTO(bookUpdated));
    }

    @ApiMessage("Delete book single image")
    @DeleteMapping("/{id}/image")
    public ResponseEntity<RestBookUpdateDTO> deleteBookSingleImageRequest(
            @PathVariable Long id,
            @RequestParam(name = "imageName", required = false) String fileName
    ) throws URISyntaxException, IOException {
        fileService.deleteSingleFile(fileName, "book");

        Book bookGetById = bookService.getBookById(id);
        List<String> currentImages = bookGetById.getImages();
        if (currentImages != null && !currentImages.isEmpty()) {
            currentImages.remove(fileName);
            bookGetById.setImages(currentImages);
        }

        Book bookUpdated = bookService.updateBookPartialById(id, bookGetById);
        return ResponseEntity.ok(bookService.convertBookToRestBookUpdateDTO(bookUpdated));
    }

    @ApiMessage("Delete book multiple image")
    @DeleteMapping("/{id}/multiple-images")
    public ResponseEntity<RestBookUpdateDTO> deleteBookMultipleImageRequest(
            @PathVariable Long id,
            @RequestBody List<String> fileNames
    ) throws URISyntaxException, IOException {
        fileService.deleteMultipleFiles(fileNames, "book");

        Book bookGetById = bookService.getBookById(id);
        List<String> currentImages = bookGetById.getImages();
        if (currentImages != null && !currentImages.isEmpty()) {
            currentImages.removeAll(fileNames);
            bookGetById.setImages(currentImages);
        }

        Book bookUpdated = bookService.updateBookPartialById(id, bookGetById);
        return ResponseEntity.ok(bookService.convertBookToRestBookUpdateDTO(bookUpdated));
    }

    @ApiMessage("Import books from file")
    @PostMapping("/import")
    public ResponseEntity<RestImportDTO<RestBookCreateDTO>> importBooksFromFileRequest(
            @Valid @RequestBody List<Book> bookList
    ) {
        RestImportDTO<RestBookCreateDTO> restImportDTO
                = bookService.importListBooksFromFile(bookList);

        HttpStatus httpStatus = HttpStatusUtil.determineHttpStatus(restImportDTO);

        return ResponseEntity
                .status(httpStatus)
                .body(restImportDTO);
    }
}
