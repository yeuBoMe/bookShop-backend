package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.dto.request.BookCreateRequest;
import com.fullstack.bookShop.dto.response.BookResponse;
import com.fullstack.bookShop.dto.response.imports.ImportFileResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.mapper.BookMapper;
import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.service.interfaces.BookService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.exception.custom.FileException;
import com.fullstack.bookShop.util.annotation.IsAdmin;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import com.fullstack.bookShop.util.status.HttpStatusUtil;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    private final BookMapper bookMapper;

    private final BookService bookService;

    @GetMapping
    @ApiMessage(ActionConstant.BOOK_FETCH_ALL)
    public ResponseEntity<PaginationResponse> fetchAllBooksRequest(
            @Filter Specification<Book> spec,
            Pageable pageable
    ) {
        return ResponseEntity.ok(bookService.fetchAllBooks(spec, pageable));
    }

    @GetMapping("/average-rating")
    @ApiMessage(ActionConstant.BOOK_FETCH_AVERAGE_RATE)
    public ResponseEntity<PaginationResponse> fetchListBooksByMinRatingGreaterThanEqualRequest(
            @RequestParam(defaultValue = "0") Double minRating,
            Pageable pageable
    ) {
        return ResponseEntity.ok(bookService.fetchListBooksByMinRatingGreaterThanEqual(minRating, pageable));
    }

    @IsAdmin
    @PostMapping
    @ApiMessage(ActionConstant.BOOK_CREATE)
    public ResponseEntity<BookResponse> createBookRequest(@Valid @RequestBody Book bookRequest) {
        Book bookCreated = bookService.createBook(bookRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookMapper.toResponse(bookCreated, ActionConstant.CREATE));
    }

    @GetMapping("/{id}")
    @ApiMessage(ActionConstant.BOOK_GET)
    public ResponseEntity<BookResponse> getBookByIdRequest(@PathVariable Long id) {
        Book bookGetById = bookService.getBookById(id);
        return ResponseEntity.ok(bookMapper.toResponse(bookGetById, ActionConstant.FETCH));
    }

    @IsAdmin
    @PutMapping("/{id}")
    @ApiMessage(ActionConstant.BOOK_UPDATE)
    public ResponseEntity<BookResponse> updateBookByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody Book bookRequest
    ) {
        Book bookUpdated = bookService.updateBookById(id, bookRequest);
        return ResponseEntity.ok(bookMapper.toResponse(bookUpdated, ActionConstant.UPDATE));
    }

    @IsAdmin
    @PatchMapping("/{id}")
    @ApiMessage(ActionConstant.BOOK_UPDATE_PARTIAL)
    public ResponseEntity<BookResponse> partialUpdateBookByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody Book bookRequest
    ) {
        Book bookPartialUpdated = bookService.partialUpdateBookById(id, bookRequest);
        return ResponseEntity.ok(bookMapper.toResponse(bookPartialUpdated, ActionConstant.UPDATE));
    }

    @IsAdmin
    @DeleteMapping("/{id}")
    @ApiMessage(ActionConstant.BOOK_DELETE)
    public ResponseEntity<Void> deleteBookByIdRequest(@PathVariable Long id) {
        bookService.deleteBookById(id);
        return ResponseEntity.noContent().build();
    }

    @IsAdmin
    @PatchMapping("/{id}/image")
    @ApiMessage(ActionConstant.BOOK_UPLOAD_SINGLE_IMAGE)
    public ResponseEntity<BookResponse> updateBookSingleImageInLocalRequest(
            @PathVariable Long id,
            @RequestParam(name = "file", required = false) MultipartFile file
    ) throws FileException,
             URISyntaxException,
             IOException {
        Book bookUpdated = bookService.uploadBookSingleImageInLocal(id, file);
        return ResponseEntity.ok(bookMapper.toResponse(bookUpdated, ActionConstant.UPDATE));
    }

    @IsAdmin
    @PatchMapping("/{id}/image/cloud")
    @ApiMessage(ActionConstant.BOOK_UPLOAD_IMAGE_CLOUDINARY)
    public ResponseEntity<BookResponse> updateBookSingleImageToCloudinaryRequest(
            @PathVariable Long id,
            @RequestParam(name = "file", required = false) MultipartFile file
    ) throws FileException, IOException {
        Book bookUpdated = bookService.uploadBookSingleImageToCloud(id, file);
        return ResponseEntity.ok(bookMapper.toResponse(bookUpdated, ActionConstant.UPDATE));
    }

    @IsAdmin
    @PatchMapping("/{id}/multiple-images")
    @ApiMessage(ActionConstant.BOOK_UPDATE_MULTIPLE_IMAGES)
    public ResponseEntity<BookResponse> updateBookMultipleImagesInLocalRequest(
            @PathVariable Long id,
            @RequestParam(name = "files", required = false) List<MultipartFile> files
    ) throws FileException,
             URISyntaxException,
             IOException {
        Book bookUpdated = bookService.uploadBookMultipleImagesInLocal(id, files);
        return ResponseEntity.ok(bookMapper.toResponse(bookUpdated, ActionConstant.UPDATE));
    }

    @IsAdmin
    @PatchMapping("/{id}/multiple-images/cloud")
    @ApiMessage(ActionConstant.BOOK_UPLOAD_MULTIPLE_IMAGES_CLOUDINARY)
    public ResponseEntity<BookResponse> updateBookMultipleImagesToCloudinaryRequest(
            @PathVariable Long id,
            @RequestParam(name = "files", required = false) List<MultipartFile> files
    ) throws FileException, IOException {
        Book bookUpdated = bookService.uploadBookMultipleImagesToCloud(id, files);
        return ResponseEntity.ok(bookMapper.toResponse(bookUpdated, ActionConstant.UPDATE));
    }

    @IsAdmin
    @DeleteMapping("/{id}/image")
    @ApiMessage(ActionConstant.BOOK_DELETE_IMAGE)
    public ResponseEntity<BookResponse> deleteBookSingleImageInLocalRequest(
            @PathVariable Long id,
            @RequestParam(name = "imageName", required = false) String fileName
    ) throws URISyntaxException, IOException {
        Book bookUpdated = bookService.removeBookSingleImageInLocal(id, fileName);
        return ResponseEntity.ok(bookMapper.toResponse(bookUpdated, ActionConstant.UPDATE));
    }

    @IsAdmin
    @DeleteMapping("/{id}/image/cloud")
    @ApiMessage(ActionConstant.BOOK_DELETE_IMAGE_CLOUDINARY)
    public ResponseEntity<BookResponse> deleteBookSingleImageInCloudRequest(
            @PathVariable Long id,
            @RequestParam(name = "imageName", required = false) String fileName
    ) throws IOException {
        Book bookUpdated = bookService.removeBookSingleImageInCloud(id, fileName);
        return ResponseEntity.ok(bookMapper.toResponse(bookUpdated, ActionConstant.UPDATE));
    }

    @IsAdmin
    @DeleteMapping("/{id}/multiple-images")
    @ApiMessage(ActionConstant.BOOK_DELETE_MULTIPLE_IMAGES)
    public ResponseEntity<BookResponse> deleteBookMultipleImageRequest(
            @PathVariable Long id,
            @Valid @RequestBody List<String> fileNames
    ) throws URISyntaxException, IOException {
        Book bookUpdated = bookService.removeBookMultipleImagesInLocal(id, fileNames);
        return ResponseEntity.ok(bookMapper.toResponse(bookUpdated, ActionConstant.UPDATE));
    }

    @IsAdmin
    @DeleteMapping("/{id}/multiple-images/cloud")
    @ApiMessage(ActionConstant.BOOK_DELETE_MULTIPLE_IMAGES_CLOUDINARY)
    public ResponseEntity<BookResponse> deleteBookMultipleImageInCloudRequest(
            @PathVariable Long id,
            @Valid @RequestBody List<String> fileNames
    ) throws Exception {
        Book bookUpdated = bookService.removeBookMultipleImagesInCloud(id, fileNames);
        return ResponseEntity.ok(bookMapper.toResponse(bookUpdated, ActionConstant.UPDATE));
    }

    @IsAdmin
    @PostMapping("/import")
    @ApiMessage(ActionConstant.BOOK_IMPORT)
    public ResponseEntity<ImportFileResponse<BookResponse>> importBooksFromFileRequest(
            @Valid @RequestBody List<@Valid BookCreateRequest> bookCreateRequestList
    ) {
        ImportFileResponse<BookResponse> restImportDTO = bookService.importBooksFromFile(bookCreateRequestList);
        HttpStatus httpStatus = HttpStatusUtil.determineHttpStatus(restImportDTO);
        return ResponseEntity
                .status(httpStatus)
                .body(restImportDTO);
    }
}
