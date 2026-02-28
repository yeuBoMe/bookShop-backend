package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.response.BookResponse;
import com.fullstack.bookShop.dto.response.imports.ImportFileResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.exception.custom.FileException;
import com.fullstack.bookShop.mapper.BookMapper;
import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.repository.BookRepository;
import com.fullstack.bookShop.repository.OrderDetailRepository;
import com.fullstack.bookShop.repository.RatingRepository;
import com.fullstack.bookShop.service.interfaces.BookService;
import com.fullstack.bookShop.service.interfaces.CategoryService;
import com.fullstack.bookShop.service.interfaces.CloudinaryService;
import com.fullstack.bookShop.service.interfaces.FileService;
import com.fullstack.bookShop.util.cloudinary.CloudinaryUtil;
import com.fullstack.bookShop.util.constant.CommonMessage;
import com.fullstack.bookShop.util.constant.FolderConstant;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import com.fullstack.bookShop.util.enums.BookEnum;
import com.fullstack.bookShop.util.enums.CategoryEnum;
import com.fullstack.bookShop.util.file.FileValidUtil;
import com.fullstack.bookShop.util.pagination.PaginationUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookServiceImpl.class);

    @Value("${bookshop.upload-file.base-uri}")
    private String baseURI;

    private final BookMapper bookMapper;

    private final BookRepository bookRepository;

    private final FileService fileService;

    private final CategoryService categoryService;

    private final RatingRepository ratingRepository;

    private final CloudinaryService cloudinaryService;

    private final OrderDetailRepository orderDetailRepository;

    @Override
    public PaginationResponse fetchAllBooks(Specification<Book> spec, Pageable pageable) {
        Page<Book> bookPage = bookRepository.findAll(spec, pageable);

        List<BookResponse> bookResponseList = bookPage.getContent()
                .stream()
                .map(book -> bookMapper.toResponse(book, ActionConstant.FETCH))
                .toList();

        PaginationResponse paginationResponse = PaginationUtil.fetchAllDataWithPagination(bookPage, pageable);
        paginationResponse.setResult(bookResponseList);
        return paginationResponse;
    }

    @Override
    public PaginationResponse fetchListBooksByMinRatingGreaterThanEqual(Double minRating, Pageable pageable) {
        Page<Book> bookPageWithAverageRating = bookRepository.findAllByMinRatingGreaterThanEqual(minRating, pageable);

        List<BookResponse> bookResponseList = bookPageWithAverageRating.getContent()
                .stream()
                .map(book -> bookMapper.toResponse(book, ActionConstant.FETCH))
                .toList();

        PaginationResponse paginationResponse =
                PaginationUtil.fetchAllDataWithPagination(bookPageWithAverageRating, pageable);
        paginationResponse.setResult(bookResponseList);
        return paginationResponse;
    }

    @Override
    public List<BookResponse> fetchAllBookResponsesFromBooks() {
        return bookRepository.findAll()
                .stream()
                .map(book -> bookMapper.toResponse(book, ActionConstant.FETCH))
                .toList();
    }

    @Transactional
    @Override
    public Book createBook(Book bookRequest) {
        LOGGER.info(
                "Creating book name={}, introduceText={}, description={}, " +
                        "reader={}, price={}, quantity={}, author={}, sold={}, category={}",
                bookRequest.getName(),
                bookRequest.getIntroduceText(),
                bookRequest.getDescription(),
                bookRequest.getReader(),
                bookRequest.getPrice(),
                bookRequest.getQuantity(),
                bookRequest.getAuthor(),
                bookRequest.getSold(),
                bookMapper.convertCategory(bookRequest)
        );

        validateAndSetCategory(bookRequest, bookRequest);
        validateBookForCreate(bookRequest);

        Book bookCreated = bookRepository.save(bookRequest);
        LOGGER.info("Create book successfully with id: {}", bookCreated.getId());

        return bookCreated;
    }

    @Transactional
    @Override
    public Book updateBookById(Long id, Book bookRequest) {
        LOGGER.info("Updating book id: {}", id);

        Book bookGetById = getBookById(id);
        validateAndSetCategory(bookGetById, bookRequest);
        validateBookForUpdate(bookGetById, bookRequest);

        bookGetById.setName(bookRequest.getName());
        bookGetById.setPrice(bookRequest.getPrice());
        bookGetById.setQuantity(bookRequest.getQuantity());
        bookGetById.setAuthor(bookRequest.getAuthor());
        bookGetById.setDescription(bookRequest.getDescription());
        bookGetById.setIntroduceText(bookRequest.getIntroduceText());
        bookGetById.setReader(bookRequest.getReader());

        LOGGER.info("Update book successfully with id: {}", id);
        return bookRepository.save(bookGetById);
    }

    @Transactional
    @Override
    public Book partialUpdateBookById(Long id, Book bookUpdated) {
        LOGGER.info("Updating book partial id: {}", id);

        Book bookGetById = getBookById(id);
        validateAndSetCategory(bookGetById, bookUpdated);

        if (bookUpdated.getName() != null && !bookUpdated.getName().trim().isEmpty()) {
            validateBookForUpdate(bookGetById, bookUpdated);
            bookGetById.setName(bookUpdated.getName());
        }
        if (bookUpdated.getPrice() != null) {
            bookGetById.setPrice(bookUpdated.getPrice());
        }
        if (bookUpdated.getQuantity() != null) {
            bookGetById.setQuantity(bookUpdated.getQuantity());
        }
        if (bookUpdated.getAuthor() != null && !bookUpdated.getAuthor().trim().isEmpty()) {
            validateBookForUpdate(bookGetById, bookUpdated);
            bookGetById.setAuthor(bookUpdated.getAuthor());
        }
        if (bookUpdated.getDescription() != null && !bookUpdated.getDescription().trim().isEmpty()) {
            bookGetById.setDescription(bookUpdated.getDescription());
        }
        if (bookUpdated.getIntroduceText() != null && !bookUpdated.getIntroduceText().trim().isEmpty()) {
            bookGetById.setIntroduceText(bookUpdated.getIntroduceText());
        }
        if (bookUpdated.getReader() != null && !bookUpdated.getReader().trim().isEmpty()) {
            bookGetById.setReader(bookUpdated.getReader());
        }

        LOGGER.info("Update book partial successfully with id: {}", id);
        return bookRepository.save(bookGetById);
    }

    @Transactional
    @Override
    public Book uploadBookSingleImageInLocal(Long id, MultipartFile file)
            throws FileException,
                   URISyntaxException,
                   IOException {

        LOGGER.info("Uploading image in local for book id={}, image={}", id, file);

        Book bookGetById = getBookById(id);
        FileValidUtil.validateSingleFile(file);
        fileService.createUploadFolder(baseURI + FolderConstant.BOOK);

        String fileUploaded = fileService.storeSingleFile(file, FolderConstant.BOOK);
        bookGetById.setImages(List.of(fileUploaded));

        LOGGER.info("Upload image in local successfully for book id: {}", id);
        return bookRepository.save(bookGetById);
    }

    @Transactional
    @Override
    public Book uploadBookSingleImageToCloud(Long id, MultipartFile file) throws FileException, IOException {
        LOGGER.info("Uploading image to cloud for book id={}, image={}", id, file);

        Book bookGetById = getBookById(id);
        FileValidUtil.validateSingleFile(file);
        Map<String, Object> filePushToCloudinary = cloudinaryService.uploadImageToCloudinary(file);

        String fileUploaded = CloudinaryUtil.getFilePushedFromCloudinary(filePushToCloudinary);
        bookGetById.setImages(List.of(fileUploaded));

        LOGGER.info("Upload image to cloud successfully for book id: {}", id);
        return bookRepository.save(bookGetById);
    }

    @Transactional
    @Override
    public Book uploadBookMultipleImagesInLocal(Long id, List<MultipartFile> fileList)
            throws FileException,
                   URISyntaxException,
                   IOException {

        LOGGER.info("Uploading multiple images in local for book id={}, size={}", id, fileList.size());

        Book bookGetById = getBookById(id);
        FileValidUtil.validateMultipleFiles(fileList);
        fileService.createUploadFolder(baseURI + FolderConstant.BOOK);

        List<String> fileNames = fileService.storeMultipleFiles(fileList, FolderConstant.BOOK);
        bookGetById.setImages(fileNames);

        LOGGER.info("Upload multiple images in local successfully for book id: {}", id);
        return bookRepository.save(bookGetById);
    }

    @Transactional
    @Override
    public Book uploadBookMultipleImagesToCloud(Long id, List<MultipartFile> fileList)
            throws FileException, IOException {

        LOGGER.info("Uploading multiple images to cloud for book id={}, size={}", id, fileList.size());

        FileValidUtil.validateMultipleFiles(fileList);
        List<String> fileNames = new ArrayList<>();

        List<Map<String, Object>> listImagesInCloud = cloudinaryService.uploadMultipleImagesToCloudinary(fileList);
        for (Map<String, Object> image : listImagesInCloud) {
            String fileUploaded = CloudinaryUtil.getFilePushedFromCloudinary(image);
            fileNames.add(fileUploaded);
        }

        Book bookGetById = getBookById(id);
        bookGetById.setImages(fileNames);

        LOGGER.info("Upload multiple images to cloud successfully for book id: {}", id);
        return bookRepository.save(bookGetById);
    }

    @Transactional
    @Override
    public Book removeBookSingleImageInLocal(Long id, String image) throws URISyntaxException, IOException {
        LOGGER.info("Removing single image in local for book id={}, image={}", id, image);

        fileService.deleteSingleFile(image, FolderConstant.BOOK);

        Book bookGetById = getBookById(id);
        bookGetById.getImages().remove(image);

        LOGGER.info("Remove image in local successfully for book id: {}", id);
        return bookRepository.save(bookGetById);
    }

    @Transactional
    @Override
    public Book removeBookSingleImageInCloud(Long id, String image) throws IOException {
        LOGGER.info("Removing single image in cloud for book id={}, image={}", id, image);

        String publicId = CloudinaryUtil.generatePublicId(image);
        cloudinaryService.deleteImageFromCloudinary(publicId);

        Book bookGetById = getBookById(id);
        bookGetById.getImages().remove(image);

        LOGGER.info("Remove image in cloud successfully for book id: {}", id);
        return bookRepository.save(bookGetById);
    }

    @Transactional
    @Override
    public Book removeBookMultipleImagesInLocal(Long id, List<String> imageList)
            throws URISyntaxException, IOException {

        LOGGER.info("Removing multiple images in local for book id={}, size={}", id, imageList.size());

        fileService.deleteMultipleFiles(imageList, FolderConstant.BOOK);

        Book bookGetById = getBookById(id);
        bookGetById.getImages().removeAll(imageList);

        LOGGER.info("Remove multiple images in local successfully for book id: {}", id);
        return bookRepository.save(bookGetById);
    }

    @Transactional
    @Override
    public Book removeBookMultipleImagesInCloud(Long id, List<String> imageList) throws Exception {
        LOGGER.info("Removing multiple images in cloud for book id={}, size={}", id, imageList.size());

        List<String> publicIdsList = CloudinaryUtil.generateListPublicIds(imageList);
        cloudinaryService.deleteMultipleImagesFromCloudinary(publicIdsList);

        Book bookGetById = getBookById(id);
        bookGetById.getImages().removeAll(imageList);

        LOGGER.info("Remove multiple images in cloud successfully for book id: {}", id);
        return bookRepository.save(bookGetById);
    }

    @Override
    public Book getBookById(Long id) {
        return bookRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        String.format(BookEnum.ID_NOT_FOUND.getMessage(), id)
                ));
    }

    @Transactional
    @Override
    public void deleteBookById(Long id) {
        LOGGER.info("Deleting book id: {}", id);
        if (!checkIdExists(id)) {
            throw new NoSuchElementException(
                    String.format(BookEnum.ID_NOT_FOUND.getMessage(), id)
            );
        }
        ratingRepository.deleteByBookId(id);
        orderDetailRepository.deleteByBookId(id);
        bookRepository.deleteById(id);
        LOGGER.info("Delete book successfully with id: {}", id);
    }

    @Override
    public boolean checkIdExists(Long id) {
        return bookRepository.existsById(id);
    }

    @Override
    public boolean checkNameAndAuthorExistsIgnoreCase(String name, String author) {
        return bookRepository.existsByNameAndAuthorIgnoreCase(name, author);
    }

    @Override
    public ImportFileResponse<BookResponse> importBooksFromFile(List<Book> bookList) {
        ImportFileResponse<BookResponse> importFileResponse = new ImportFileResponse<>();
        List<BookResponse> successData = new ArrayList<>();
        List<ImportFileResponse.DetailFaultInfo<BookResponse>> detailFaultInfoList = new ArrayList<>();

        int countSuccess = 0;
        int countError = 0;

        for (int i = 0; i < bookList.size(); i++) {
            Book book = bookList.get(i);
            BookResponse bookResponse = bookMapper.toResponse(book, ActionConstant.CREATE);

            try {
                validateAndSetCategory(book, book);
                validateBookForCreate(book);
                bookRepository.save(book);

                successData.add(bookResponse);
                countSuccess++;

            } catch (ConstraintViolationException e) {
                List<String> errorMessage = e.getConstraintViolations()
                        .stream()
                        .map(ConstraintViolation::getMessage)
                        .toList();

                detailFaultInfoList.add(new ImportFileResponse.DetailFaultInfo<>(
                        i + 1,
                        errorMessage,
                        bookResponse
                ));

                countError++;

            } catch (Exception e) {
                detailFaultInfoList.add(new ImportFileResponse.DetailFaultInfo<>(
                        i + 1,
                        e.getMessage(),
                        bookResponse
                ));

                countError++;
            }
        }

        importFileResponse.setCountSuccess(countSuccess);
        importFileResponse.setCountError(countError);
        importFileResponse.setDetail(countError > 0 ? detailFaultInfoList : CommonMessage.IMPORT_SUCCESS);
        importFileResponse.setItems(successData);

        return importFileResponse;
    }

    private void validateBookForCreate(Book bookRequest) {
        if (checkNameAndAuthorExistsIgnoreCase(bookRequest.getName(), bookRequest.getAuthor())) {
            throw new IllegalArgumentException(
                    String.format(
                            BookEnum.NAME_AUTHOR_EXISTS.getMessage(),
                            bookRequest.getName(),
                            bookRequest.getAuthor()
                    )
            );
        }
    }

    private void validateBookForUpdate(Book currentBook, Book bookRequest) {
        boolean isNameAndAuthorExists = checkNameAndAuthorExistsIgnoreCase(
                bookRequest.getName(),
                bookRequest.getAuthor()
        );

        if (isNameAndAuthorExists && !(
                currentBook.getName().equals(bookRequest.getName())
                        && currentBook.getAuthor().equals(bookRequest.getAuthor())
        )) {
            throw new IllegalArgumentException(
                    String.format(
                            BookEnum.NAME_AUTHOR_EXISTS.getMessage(),
                            bookRequest.getName(),
                            bookRequest.getAuthor()
                    )
            );
        }
    }

    private void validateAndSetCategory(Book currentBook, Book bookRequest) {
        if (bookRequest.getCategory() != null) {
            Long categoryId = bookRequest.getCategory().getId();
            if (!categoryService.checkIdExists(categoryId)) {
                throw new NoSuchElementException(
                        String.format(CategoryEnum.ID_NOT_FOUND.getMessage(), categoryId)
                );
            }
            Category categoryGetById = categoryService.getCategoryById(categoryId);
            currentBook.setCategory(categoryGetById);
        }
    }
}
