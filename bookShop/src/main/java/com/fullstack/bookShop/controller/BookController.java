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
public class BookController {

    @Value("${bookshop.upload-file.base-uri}")
    private String baseUri;

    private final BookService bookService;

    private final FileService fileService;

    public BookController(BookService bookService, FileService fileService) {
        this.bookService = bookService;
        this.fileService = fileService;
    }

    @ApiMessage("Create book")
    @PostMapping
    public ResponseEntity<RestBookCreateDTO> createBookRequest(@Valid @RequestBody Book book) {
        Book bookCreated = this.bookService.createBook(book);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.bookService.convertBookToRestBookCreateDTO(bookCreated));
    }

    @ApiMessage("Fetch list books")
    @GetMapping
    public ResponseEntity<ResultPaginationDTO> fetchListBooksRequest(
            @Filter Specification<Book> spec,
            Pageable pageable
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.bookService.fetchListBooks(spec, pageable));
    }

    @ApiMessage("Get book")
    @GetMapping("/{id}")
    public ResponseEntity<RestBookViewDTO> getBookByIdRequest(@PathVariable Long id) {
        Book bookGetById = this.bookService.getBookById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.bookService.convertBookToRestBookViewDTO(bookGetById));
    }

    @ApiMessage("Update book")
    @PutMapping("/{id}")
    public ResponseEntity<RestBookUpdateDTO> updateBookByIdRequest(
            @Valid @RequestBody Book book,
            @PathVariable Long id
    ) {
        Book bookUpdated = this.bookService.updateBookById(id, book);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.bookService.convertBookToRestBookUpdateDTO(bookUpdated));
    }

    @ApiMessage("Update book partial")
    @PatchMapping("/{id}")
    public ResponseEntity<RestBookUpdateDTO> partialUpdateBookByIdRequest(
            @Valid @RequestBody Book book,
            @PathVariable Long id
    ) {
        Book bookUpdated = this.bookService.updateBookPartialById(id, book);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.bookService.convertBookToRestBookUpdateDTO(bookUpdated));
    }

    @ApiMessage("Delete book")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookByIdRequest(@PathVariable Long id) {
        this.bookService.deleteBookById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(null);
    }

    @ApiMessage("Update book image")
    @PatchMapping("/{id}/image")
    public ResponseEntity<RestBookUpdateDTO> updateBookImageRequest(
            @PathVariable Long id,
            @RequestParam(name = "fileName") MultipartFile fileName,
            @RequestParam(name = "folder") String folder
    ) throws
            StorageException,
            URISyntaxException,
            IOException {

        FileValidUtil.validateFile(fileName);
        this.fileService.createUploadFolder(this.baseUri + folder);
        String fileUploaded =  this.fileService.storeSingleFile(fileName, folder);

        Book book = new Book();
        book.setImages(List.of(fileUploaded));
        Book bookUpdated = this.bookService.updateBookPartialById(id, book);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bookService.convertBookToRestBookUpdateDTO(bookUpdated));
    }

    @ApiMessage("Update book multiple images")
    @PatchMapping("/{id}/multiple-images")
    public ResponseEntity<RestBookUpdateDTO> updateBookMultipleImageRequest(
            @PathVariable Long id,
            @RequestParam(name = "fileNames") List<MultipartFile> files,
            @RequestParam(name = "folder") String folder
    ) throws
            StorageException,
            URISyntaxException,
            IOException {

        FileValidUtil.validateListFiles(files);
        this.fileService.createUploadFolder(this.baseUri + folder);
        List<String> fileNames = this.fileService.storeMultipleFiles(files, folder);

        Book book = new Book();
        book.setImages(fileNames);
        Book bookUpdated = this.bookService.updateBookPartialById(id, book);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bookService.convertBookToRestBookUpdateDTO(bookUpdated));
    }

    @ApiMessage("Import books from file")
    @PostMapping("/import")
    public ResponseEntity<RestImportDTO<RestBookCreateDTO>> importBooksFromFileRequest(
            @Valid @RequestBody List<RestBookCreateDTO> bookCreateDTOList
    ) {
        RestImportDTO<RestBookCreateDTO> restImportDTO =
                this.bookService.importListBooksFromFile(bookCreateDTOList);

        HttpStatus httpStatus = HttpStatusUtil.determineHttpStatus(restImportDTO);

        return ResponseEntity
                .status(httpStatus)
                .body(restImportDTO);
    }
}
