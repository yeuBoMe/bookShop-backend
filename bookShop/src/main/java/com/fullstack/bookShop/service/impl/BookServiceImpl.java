package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.domain.dto.book.RestBookCreateDTO;
import com.fullstack.bookShop.domain.dto.book.RestBookUpdateDTO;
import com.fullstack.bookShop.domain.dto.book.RestBookViewDTO;
import com.fullstack.bookShop.domain.dto.imports.RestImportDTO;
import com.fullstack.bookShop.domain.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.domain.entity.Book;
import com.fullstack.bookShop.domain.entity.Category;
import com.fullstack.bookShop.repository.BookRepository;
import com.fullstack.bookShop.service.BookService;
import com.fullstack.bookShop.service.CategoryService;
import com.fullstack.bookShop.util.pagination.PageUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
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
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    private final CategoryService categoryService;

    public BookServiceImpl(BookRepository bookRepository, CategoryService categoryService) {
        this.bookRepository = bookRepository;
        this.categoryService = categoryService;
    }

    private void checkNullAndSetCategory(Book bookRequest, Book currentBook) {
        if (bookRequest.getCategory() != null) {
            if (!this.categoryService.checkIdExists(bookRequest.getCategory().getId())) {
                throw new NoSuchElementException("Category with id '" + bookRequest.getCategory().getId() + "' not exist");
            }
            Category categoryGetById = this.categoryService.getCategoryById(bookRequest.getCategory().getId());
            currentBook.setCategory(categoryGetById);
        }
    }

    @Transactional
    @Override
    public Book createBook(Book book) {
        if (this.checkNameAndAuthorExistsIgnoreCase(book.getName(), book.getAuthor())) {
            throw new IllegalArgumentException(
                    String.format("Book with name '%s' and author '%s' already exists!",
                            book.getName(),
                            book.getAuthor()
                    )
            );
        }

        this.checkNullAndSetCategory(book, book);
        return this.bookRepository.save(book);
    }

    @Transactional
    @Override
    public Book updateBookById(Long id, Book bookUpdated) {
        Book bookGetById = this.getBookById(id);

        if (this.checkNameAndAuthorExistsIgnoreCase(bookUpdated.getName(), bookUpdated.getAuthor())
                && !(bookGetById.getName().equals(bookUpdated.getName())
                && bookGetById.getAuthor().equals(bookUpdated.getAuthor()))
        ) {
            throw new IllegalArgumentException(
                    String.format("Book with name '%s' and author '%s' already exists!",
                            bookUpdated.getName(),
                            bookUpdated.getAuthor()
                    )
            );
        }

        this.checkNullAndSetCategory(bookUpdated, bookGetById);

        bookGetById.setName(bookUpdated.getName());
        bookGetById.setPrice(bookUpdated.getPrice());
        bookGetById.setQuantity(bookUpdated.getQuantity());
        bookGetById.setAuthor(bookUpdated.getAuthor());
        bookGetById.setDescription(bookUpdated.getDescription());
        bookGetById.setIntroduceText(bookUpdated.getIntroduceText());
        bookGetById.setReader(bookUpdated.getReader());
        return this.bookRepository.save(bookGetById);
    }

    @Transactional
    @Override
    public Book updateBookPartialById(Long id, Book bookUpdated) {
        Book bookGetById = this.getBookById(id);

        if (this.checkNameAndAuthorExistsIgnoreCase(bookUpdated.getName(), bookUpdated.getAuthor())
                && !(bookGetById.getName().equals(bookUpdated.getName())
                && bookGetById.getAuthor().equals(bookUpdated.getAuthor()))
        ) {
            throw new IllegalArgumentException(
                    String.format("Book with name '%s' and author '%s' already exists!",
                            bookUpdated.getName(),
                            bookUpdated.getAuthor()
                    )
            );
        }

        if (bookUpdated.getName() != null && !bookUpdated.getName().isEmpty()) {
            bookGetById.setName(bookUpdated.getName());
        }

        if (bookUpdated.getPrice() != null) {
            bookGetById.setPrice(bookUpdated.getPrice());
        }

        if (bookUpdated.getQuantity() != null) {
            bookGetById.setQuantity(bookUpdated.getQuantity());
        }

        if (bookUpdated.getAuthor() != null) {
            bookGetById.setAuthor(bookUpdated.getAuthor());
        }

        if (bookUpdated.getDescription() != null) {
            bookGetById.setDescription(bookUpdated.getDescription());
        }

        if (bookUpdated.getIntroduceText() != null) {
            bookGetById.setIntroduceText(bookUpdated.getIntroduceText());
        }

        if (bookUpdated.getReader() != null) {
            bookGetById.setReader(bookUpdated.getReader());
        }

        if (bookUpdated.getImages() != null && !bookUpdated.getImages().isEmpty()) {
            System.out.println("Updating book image to: " + bookUpdated.getImages());
            bookGetById.setImages(bookUpdated.getImages());
        }

        this.checkNullAndSetCategory(bookUpdated, bookGetById);
        return this.bookRepository.save(bookGetById);
    }

    @Override
    public Book getBookById(Long id) {
        return this.bookRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException("Book with id '" + id + "' not found"));
    }

    @Transactional
    @Override
    public void deleteBookById(Long id) {
        if (!this.checkIdExists(id)) {
            throw new NoSuchElementException("Book with id '" + id + "' not found");
        }
        this.bookRepository.deleteById(id);
    }

    @Override
    public boolean checkIdExists(Long id) {
        return this.bookRepository.existsById(id);
    }

    @Override
    public boolean checkNameAndAuthorExistsIgnoreCase(String name, String author) {
        return this.bookRepository.existsByNameAndAuthorIgnoreCase(name, author);
    }

    @Override
    public RestBookCreateDTO convertBookToRestBookCreateDTO(Book book) {
        String categoryName = Optional.ofNullable(book.getCategory())
                .map(Category::getName)
                .orElse(null);
        return new RestBookCreateDTO(
                book.getId(),
                book.getName(),
                categoryName,
                book.getPrice(),
                book.getQuantity(),
                book.getImages(),
                book.getAuthor(),
                book.getIntroduceText(),
                book.getDescription(),
                book.getReader(),
                book.getCreatedAt(),
                book.getCreatedBy()
        );
    }

    @Override
    public RestBookUpdateDTO convertBookToRestBookUpdateDTO(Book book) {
        String categoryName = Optional.ofNullable(book.getCategory())
                .map(Category::getName)
                .orElse(null);
        return new RestBookUpdateDTO(
                book.getId(),
                book.getName(),
                categoryName,
                book.getPrice(),
                book.getQuantity(),
                book.getImages(),
                book.getAuthor(),
                book.getIntroduceText(),
                book.getDescription(),
                book.getReader(),
                book.getUpdatedAt(),
                book.getUpdatedBy()
        );
    }

    @Override
    public RestBookViewDTO convertBookToRestBookViewDTO(Book book) {
        String categoryName = Optional.ofNullable(book.getCategory())
                .map(Category::getName)
                .orElse(null);
        return RestBookViewDTO.builder()
                .id(book.getId())
                .name(book.getName())
                .category(categoryName)
                .price(book.getPrice())
                .quantity(book.getQuantity())
                .images(book.getImages())
                .author(book.getAuthor())
                .introduceText(book.getIntroduceText())
                .description(book.getDescription())
                .reader(book.getReader())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .createdBy(book.getCreatedBy())
                .updatedBy(book.getUpdatedBy())
                .build();
    }

    @Override
    public ResultPaginationDTO fetchListBooks(Specification<Book> spec, Pageable pageable) {
        Page<Book> bookPage = this.bookRepository.findAll(spec, pageable);

        List<RestBookViewDTO> restBookViewDTOList = bookPage.getContent()
                .stream()
                .map(this::convertBookToRestBookViewDTO)
                .toList();

        ResultPaginationDTO resultPaginationDTO = PageUtil.handleFetchAllDataWithPagination(bookPage, pageable);
        resultPaginationDTO.setResult(restBookViewDTOList);
        return resultPaginationDTO;
    }

    @Override
    public RestImportDTO<RestBookCreateDTO> importListBooksFromFile(List<Book> bookList) {
        RestImportDTO<RestBookCreateDTO> restImportDTO = new RestImportDTO<>();
        List<RestBookCreateDTO> successData = new ArrayList<>();
        List<RestImportDTO.DetailFaultInfo<RestBookCreateDTO>> detailFaultInfoList = new ArrayList<>();

        int countSuccess = 0;
        int countError = 0;

        for (int i = 0; i < bookList.size(); i++) {
            Book book = bookList.get(i);

            try {
                if (this.checkNameAndAuthorExistsIgnoreCase(book.getName(), book.getAuthor())) {
                    throw new IllegalArgumentException(
                            String.format("Book with name '%s' and author '%s' already exists!",
                                    book.getName(),
                                    book.getAuthor()
                            )
                    );
                }

                this.checkNullAndSetCategory(book, book);
                this.bookRepository.save(book);

                RestBookCreateDTO restBookCreateDTO = this.convertBookToRestBookCreateDTO(book);
                successData.add(restBookCreateDTO);
                countSuccess++;

            } catch (ConstraintViolationException e) {
                RestBookCreateDTO restBookCreateDTO = this.convertBookToRestBookCreateDTO(book);
                countError++;
                List<String> errorMessage = e.getConstraintViolations().stream()
                        .map(ConstraintViolation::getMessage)
                        .toList();
                detailFaultInfoList.add(new RestImportDTO.DetailFaultInfo<>(
                        i + 1,
                        errorMessage,
                        restBookCreateDTO
                ));

            } catch (Exception e) {
                RestBookCreateDTO restBookCreateDTO = this.convertBookToRestBookCreateDTO(book);
                countError++;
                detailFaultInfoList.add(new RestImportDTO.DetailFaultInfo<>(
                        i + 1,
                        e.getMessage(),
                        restBookCreateDTO
                ));
            }
        }

        restImportDTO.setCountSuccess(countSuccess);
        restImportDTO.setCountError(countError);
        restImportDTO.setDetail(countError > 0 ? detailFaultInfoList : "Import books successfully!");
        restImportDTO.setItems(successData);

        return restImportDTO;
    }
}
