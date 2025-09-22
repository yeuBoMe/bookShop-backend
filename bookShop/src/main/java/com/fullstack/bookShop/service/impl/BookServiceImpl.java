package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.domain.dto.book.RestBookCreateDTO;
import com.fullstack.bookShop.domain.dto.book.RestBookImportDTO;
import com.fullstack.bookShop.domain.dto.book.RestBookUpdateDTO;
import com.fullstack.bookShop.domain.dto.book.RestBookViewDTO;
import com.fullstack.bookShop.domain.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.domain.entity.Book;
import com.fullstack.bookShop.repository.BookRepository;
import com.fullstack.bookShop.service.BookService;
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

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional
    @Override
    public Book createBook(Book book) {
        if (this.checkNameAndAuthorExists(book.getName(), book.getAuthor())) {
            throw new IllegalArgumentException(
                    String.format("Book with name '%s' and author '%s' already exists!",
                            book.getName(),
                            book.getAuthor()
                    )
            );
        }
        return this.bookRepository.save(book);
    }

    @Transactional
    @Override
    public Book updateBookById(Long id, Book bookUpdated) {
        Book bookGetById = this.getBookById(id);

        if (this.checkNameAndAuthorExists(bookUpdated.getName(), bookUpdated.getAuthor())
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

        bookGetById.setName(bookUpdated.getName());
        bookGetById.setCategory(bookUpdated.getCategory());
        bookGetById.setPrice(bookUpdated.getPrice());
        bookGetById.setQuantity(bookUpdated.getQuantity());
        bookGetById.setAuthor(bookUpdated.getAuthor());
        return this.bookRepository.save(bookGetById);
    }

    @Transactional
    @Override
    public Book updateBookPartialById(Long id, Book bookUpdated) {
        Book bookGetById = this.getBookById(id);

        if (this.checkNameAndAuthorExists(bookUpdated.getName(), bookUpdated.getAuthor())
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

        if (bookUpdated.getCategory() != null && !bookUpdated.getCategory().isEmpty()) {
            bookGetById.setCategory(bookUpdated.getCategory());
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
    public boolean checkNameAndAuthorExists(String name, String author) {
        return this.bookRepository.existsByNameAndAuthor(name, author);
    }

    @Override
    public RestBookCreateDTO convertBookToRestBookCreateDTO(Book book) {
        return new RestBookCreateDTO(
                book.getId(),
                book.getName(),
                book.getCategory(),
                book.getPrice(),
                book.getQuantity(),
                book.getImage(),
                book.getAuthor(),
                book.getCreatedAt(),
                book.getCreatedBy()
        );
    }

    @Override
    public RestBookUpdateDTO convertBookToRestBookUpdateDTO(Book book) {
        return new RestBookUpdateDTO(
                book.getId(),
                book.getName(),
                book.getCategory(),
                book.getPrice(),
                book.getQuantity(),
                book.getImage(),
                book.getAuthor(),
                book.getUpdatedAt(),
                book.getUpdatedBy()
        );
    }

    @Override
    public RestBookViewDTO convertBookToRestBookViewDTO(Book book) {
        return RestBookViewDTO.builder()
                .id(book.getId())
                .name(book.getName())
                .category(book.getCategory())
                .price(book.getPrice())
                .quantity(book.getQuantity())
                .image(book.getImage())
                .author(book.getAuthor())
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
    public RestBookImportDTO importListBooksFromFile(List<RestBookCreateDTO> bookCreateDTOList) {
        RestBookImportDTO restBookImportDTO = new RestBookImportDTO();
        List<RestBookCreateDTO> successData = new ArrayList<>();
        List<RestBookImportDTO.DetailFaultInfo> detailFaultInfoList = new ArrayList<>();

        int countSuccess = 0;
        int countError = 0;

        for (int i = 0; i < bookCreateDTOList.size(); i++) {
            RestBookCreateDTO restBookCreateDTO = bookCreateDTOList.get(i);

            try {
                if (this.checkNameAndAuthorExists(restBookCreateDTO.getName(), restBookCreateDTO.getAuthor())) {
                    throw new IllegalArgumentException(
                            String.format("Book with name '%s' and author '%s' already exists!",
                                    restBookCreateDTO.getName(),
                                    restBookCreateDTO.getAuthor()
                            )
                    );
                }

                Book book = this.buildBookFromRestBookCreateDTO(restBookCreateDTO);
                RestBookCreateDTO bookDTO = this.convertBookToRestBookCreateDTO(book);
                successData.add(bookDTO);
                countSuccess++;

            } catch (ConstraintViolationException e) {
                countError++;
                List<String> errorMessage = e.getConstraintViolations().stream()
                        .map(ConstraintViolation::getMessage)
                        .toList();
                detailFaultInfoList.add(new RestBookImportDTO.DetailFaultInfo(
                        i + 1,
                        errorMessage,
                        restBookCreateDTO
                ));

            } catch (Exception e) {
                countError++;
                detailFaultInfoList.add(new RestBookImportDTO.DetailFaultInfo(
                        i + 1,
                        e.getMessage(),
                        restBookCreateDTO
                ));
            }
        }

        restBookImportDTO.setCountSuccess(countSuccess);
        restBookImportDTO.setCountError(countError);
        restBookImportDTO.setDetail(countError > 0 ? detailFaultInfoList : "Import books successfully!");
        restBookImportDTO.setBooks(successData);
        return restBookImportDTO;
    }

    private Book buildBookFromRestBookCreateDTO(RestBookCreateDTO restBookCreateDTO) {
        Book book = new Book();
        book.setName(restBookCreateDTO.getName());
        book.setCategory(restBookCreateDTO.getCategory());
        book.setPrice(restBookCreateDTO.getPrice());
        book.setQuantity(restBookCreateDTO.getQuantity());
        book.setAuthor(restBookCreateDTO.getAuthor());
        return this.bookRepository.save(book);
    }
}
