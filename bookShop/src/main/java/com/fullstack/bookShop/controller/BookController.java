package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.dto.book.RestBookCreateDTO;
import com.fullstack.bookShop.dto.book.RestBookUpdateDTO;
import com.fullstack.bookShop.dto.book.RestBookViewDTO;
import com.fullstack.bookShop.dto.imports.RestImportDTO;
import com.fullstack.bookShop.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.service.BookService;
import com.fullstack.bookShop.service.FileService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.exception.custom.StorageException;
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

    @PostMapping
    @ApiMessage("Create book")
    public ResponseEntity<RestBookCreateDTO> createBookRequest(@Valid @RequestBody Book book) {
        Book bookCreated = bookService.createBook(book);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookService.convertBookToRestBookCreateDTO(bookCreated));
    }

    @GetMapping
    @ApiMessage("Fetch list books")
    public ResponseEntity<ResultPaginationDTO> fetchListBooksRequest(
            @Filter Specification<Book> spec,
            Pageable pageable
    ) {
        return ResponseEntity.ok(bookService.fetchListBooks(spec, pageable));
    }

    @GetMapping("/average-rating")
    @ApiMessage("Fetch list books by average rating")
    public ResponseEntity<ResultPaginationDTO> fetchListBooksByMinRatingGreaterThaneEqualRequest(
            @RequestParam(defaultValue = "0") Double minRating,
            Pageable pageable
    ) {
        return ResponseEntity.ok(bookService.fetchListBooksByMinRatingGreaterThanEqual(minRating, pageable));
    }

    @GetMapping("/{id}")
    @ApiMessage("Get book")
    public ResponseEntity<RestBookViewDTO> getBookByIdRequest(@PathVariable Long id) {
        Book bookGetById = bookService.getBookById(id);
        return ResponseEntity.ok(bookService.convertBookToRestBookViewDTO(bookGetById));
    }

    @PutMapping("/{id}")
    @ApiMessage("Update book")
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

    @DeleteMapping("/{id}")
    @ApiMessage("Delete book")
    public ResponseEntity<Void> deleteBookByIdRequest(@PathVariable Long id) {
        bookService.deleteBookById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/image")
    @ApiMessage("Update book image")
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

    @PatchMapping("/{id}/multiple-images")
    @ApiMessage("Update book multiple images")
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

    @DeleteMapping("/{id}/image")
    @ApiMessage("Delete book single image")
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

    @DeleteMapping("/{id}/multiple-images")
    @ApiMessage("Delete book multiple image")
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

    @PostMapping("/import")
    @ApiMessage("Import books from file")
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
