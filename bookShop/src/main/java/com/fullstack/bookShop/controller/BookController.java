package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.dto.response.BookResponse;
import com.fullstack.bookShop.dto.response.imports.ImportFileResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.mapper.BookMapper;
import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.service.interfaces.BookService;
import com.fullstack.bookShop.service.interfaces.CloudinaryService;
import com.fullstack.bookShop.service.interfaces.FileService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.exception.custom.FileException;
import com.fullstack.bookShop.util.cloudinary.CloudinaryUtil;
import com.fullstack.bookShop.util.constant.auth.AuthorityConstant;
import com.fullstack.bookShop.util.constant.FolderConstant;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import com.fullstack.bookShop.util.file.FileValidUtil;
import com.fullstack.bookShop.util.status.HttpStatusUtil;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    @Value("${bookshop.upload-file.base-uri}")
    private String baseURI;

    private final BookMapper bookMapper;

    private final BookService bookService;

    private final FileService fileService;

    private final CloudinaryService cloudinaryService;

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

    @PreAuthorize("hasRole('" + AuthorityConstant.ADMIN + "')")
    @PostMapping
    @ApiMessage(ActionConstant.BOOK_CREATE)
    public ResponseEntity<BookResponse> createBookRequest(@Valid @RequestBody Book bookRequest) {
        Book bookCreated = bookService.createBook(bookRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookMapper.convertBookToCreateResponse(bookCreated));
    }

    @GetMapping("/{id}")
    @ApiMessage(ActionConstant.BOOK_GET)
    public ResponseEntity<BookResponse> getBookByIdRequest(@PathVariable Long id) {
        Book bookGetById = bookService.getBookById(id);
        return ResponseEntity.ok(bookMapper.convertBookToViewResponse(bookGetById));
    }

    @PreAuthorize("hasRole('" + AuthorityConstant.ADMIN + "')")
    @PutMapping("/{id}")
    @ApiMessage(ActionConstant.BOOK_UPDATE)
    public ResponseEntity<BookResponse> updateBookByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody Book bookRequest
    ) {
        Book bookUpdated = bookService.updateBookById(id, bookRequest);
        return ResponseEntity.ok(bookMapper.convertBookToUpdateResponse(bookUpdated));
    }

    @PreAuthorize("hasRole('" + AuthorityConstant.ADMIN + "')")
    @PatchMapping("/{id}")
    @ApiMessage(ActionConstant.BOOK_UPDATE_PARTIAL)
    public ResponseEntity<BookResponse> partialUpdateBookByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody Book bookRequest
    ) {
        Book bookPartialUpdated = bookService.partialUpdateBookById(id, bookRequest);
        return ResponseEntity.ok(bookMapper.convertBookToUpdateResponse(bookPartialUpdated));
    }

    @PreAuthorize("hasRole('" + AuthorityConstant.ADMIN + "')")
    @DeleteMapping("/{id}")
    @ApiMessage(ActionConstant.BOOK_DELETE)
    public ResponseEntity<Void> deleteBookByIdRequest(@PathVariable Long id) {
        bookService.deleteBookById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('" + AuthorityConstant.ADMIN + "')")
    @PatchMapping("/{id}/image")
    @ApiMessage(ActionConstant.BOOK_UPLOAD_SINGLE_IMAGE)
    public ResponseEntity<BookResponse> updateBookSingleImageRequest(
            @PathVariable Long id,
            @RequestParam(name = "file", required = false) MultipartFile file
    ) throws FileException,
             URISyntaxException,
             IOException
    {
        FileValidUtil.validateSingleFile(file);
        fileService.createUploadFolder(baseURI + FolderConstant.BOOK);

        String fileUploaded = fileService.storeSingleFile(file, FolderConstant.BOOK);
        Book bookUpdated = bookService.uploadBookSingleImage(id, fileUploaded);

        return ResponseEntity.ok(bookMapper.convertBookToUpdateResponse(bookUpdated));
    }

    @PreAuthorize("hasRole('" + AuthorityConstant.ADMIN + "')")
    @PatchMapping("/{id}/image/cloud")
    @ApiMessage(ActionConstant.BOOK_UPLOAD_IMAGE_CLOUDINARY)
    public ResponseEntity<BookResponse> updateBookSingleImageToCloudinaryRequest(
            @PathVariable Long id,
            @RequestParam(name = "file", required = false) MultipartFile file
    ) throws FileException, IOException {

        FileValidUtil.validateSingleFile(file);
        Map<String, Object> imageUploadedInCloud = cloudinaryService.uploadImageToCloudinary(file);

        String fileUploaded = cloudinaryService.getFilePushedFromCloudinary(imageUploadedInCloud);
        Book bookUpdated = bookService.uploadBookSingleImage(id, fileUploaded);

        return ResponseEntity.ok(bookMapper.convertBookToUpdateResponse(bookUpdated));
    }

    @PreAuthorize("hasRole('" + AuthorityConstant.ADMIN + "')")
    @PatchMapping("/{id}/multiple-images")
    @ApiMessage(ActionConstant.BOOK_UPDATE_MULTIPLE_IMAGES)
    public ResponseEntity<BookResponse> updateBookMultipleImagesRequest(
            @PathVariable Long id,
            @RequestParam(name = "files", required = false) List<MultipartFile> files
    ) throws FileException,
             URISyntaxException,
             IOException
    {
        FileValidUtil.validateMultipleFiles(files);
        fileService.createUploadFolder(baseURI + FolderConstant.BOOK);

        List<String> fileNames = fileService.storeMultipleFiles(files, FolderConstant.BOOK);
        Book bookUpdated = bookService.uploadBookMultipleImages(id, fileNames);

        return ResponseEntity.ok(bookMapper.convertBookToUpdateResponse(bookUpdated));
    }

    @PreAuthorize("hasRole('" + AuthorityConstant.ADMIN + "')")
    @PatchMapping("/{id}/multiple-images/cloud")
    @ApiMessage(ActionConstant.BOOK_UPLOAD_MULTIPLE_IMAGES_CLOUDINARY)
    public ResponseEntity<BookResponse> updateBookMultipleImagesToCloudinaryRequest(
            @PathVariable Long id,
            @RequestParam(name = "files", required = false) List<MultipartFile> files
    ) throws FileException, IOException {

        FileValidUtil.validateMultipleFiles(files);
        List<String> fileNames = new ArrayList<>();

        List<Map<String, Object>> listImagesInCloud = cloudinaryService.uploadMultipleImagesToCloudinary(files);
        for (Map<String, Object> imageUploadedInCloud : listImagesInCloud) {
            String fileUploaded = cloudinaryService.getFilePushedFromCloudinary(imageUploadedInCloud);
            fileNames.add(fileUploaded);
        }

        Book bookUpdated = bookService.uploadBookMultipleImages(id, fileNames);
        return ResponseEntity.ok(bookMapper.convertBookToUpdateResponse(bookUpdated));
    }

    @PreAuthorize("hasRole('" + AuthorityConstant.ADMIN + "')")
    @DeleteMapping("/{id}/image")
    @ApiMessage(ActionConstant.BOOK_DELETE_IMAGE)
    public ResponseEntity<BookResponse> deleteBookSingleImageRequest(
            @PathVariable Long id,
            @RequestParam(name = "imageName", required = false) String fileName
    ) throws URISyntaxException, IOException {

        fileService.deleteSingleFile(fileName, FolderConstant.BOOK);
        Book bookUpdated = bookService.removeBookSingleImage(id, fileName);

        return ResponseEntity.ok(bookMapper.convertBookToUpdateResponse(bookUpdated));
    }

    @PreAuthorize("hasRole('" + AuthorityConstant.ADMIN + "')")
    @DeleteMapping("/{id}/image/cloud")
    @ApiMessage(ActionConstant.BOOK_DELETE_IMAGE_CLOUDINARY)
    public ResponseEntity<BookResponse> deleteBookSingleImageInCloudRequest(
            @PathVariable Long id,
            @RequestParam(name = "imageName", required = false) String fileName
    ) throws IOException {

        String publicId = CloudinaryUtil.generatePublicId(fileName);
        cloudinaryService.deleteImageFromCloudinary(publicId);

        Book bookUpdated = bookService.removeBookSingleImage(id, fileName);
        return ResponseEntity.ok(bookMapper.convertBookToUpdateResponse(bookUpdated));
    }

    @PreAuthorize("hasRole('" + AuthorityConstant.ADMIN + "')")
    @DeleteMapping("/{id}/multiple-images")
    @ApiMessage(ActionConstant.BOOK_DELETE_MULTIPLE_IMAGES)
    public ResponseEntity<BookResponse> deleteBookMultipleImageRequest(
            @PathVariable Long id,
            @RequestBody List<String> fileNames
    ) throws URISyntaxException, IOException {

        fileService.deleteMultipleFiles(fileNames, FolderConstant.BOOK);
        Book bookUpdated = bookService.removeBookMultipleImages(id, fileNames);

        return ResponseEntity.ok(bookMapper.convertBookToUpdateResponse(bookUpdated));
    }

    @PreAuthorize("hasRole('" + AuthorityConstant.ADMIN + "')")
    @DeleteMapping("/{id}/multiple-images/cloud")
    @ApiMessage(ActionConstant.BOOK_DELETE_MULTIPLE_IMAGES_CLOUDINARY)
    public ResponseEntity<BookResponse> deleteBookMultipleImageInCloudRequest(
            @PathVariable Long id,
            @RequestBody List<String> fileNames
    ) throws Exception {

        List<String> publicIds = CloudinaryUtil.generateListPublicIds(fileNames);
        cloudinaryService.deleteMultipleImagesFromCloudinary(publicIds);

        Book bookUpdated = bookService.removeBookMultipleImages(id, fileNames);
        return ResponseEntity.ok(bookMapper.convertBookToUpdateResponse(bookUpdated));
    }

    @PreAuthorize("hasRole('" + AuthorityConstant.ADMIN + "')")
    @PostMapping("/import")
    @ApiMessage(ActionConstant.BOOK_IMPORT)
    public ResponseEntity<ImportFileResponse<BookResponse>> importBooksFromFileRequest(
            @Valid @RequestBody List<Book> bookList
    ) {
        ImportFileResponse<BookResponse> restImportDTO = bookService.importBooksFromFile(bookList);
        HttpStatus httpStatus = HttpStatusUtil.determineHttpStatus(restImportDTO);
        return ResponseEntity
                .status(httpStatus)
                .body(restImportDTO);
    }
}
