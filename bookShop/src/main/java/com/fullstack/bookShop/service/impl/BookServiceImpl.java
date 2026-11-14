package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.request.BookCreateRequest;
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
import com.fullstack.bookShop.specification.BookSpecification;
import com.fullstack.bookShop.util.cloudinary.CloudinaryUtil;
import com.fullstack.bookShop.util.constant.CommonMessage;
import com.fullstack.bookShop.util.constant.FolderConstant;
import com.fullstack.bookShop.util.constant.SymbolConstant;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import com.fullstack.bookShop.util.enums.BookEnum;
import com.fullstack.bookShop.util.enums.CategoryEnum;
import com.fullstack.bookShop.util.file.FileValidUtil;
import com.fullstack.bookShop.util.pagination.PaginationUtil;
import com.fullstack.bookShop.util.message.MessageTranslator;
import com.fullstack.bookShop.util.string.StringUtils;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookServiceImpl.class);

    private final MessageTranslator messageTranslator;

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
        PaginationResponse paginationResponse = PaginationUtil.fetchAllDataWithPagination(bookPage, pageable);

        List<BookResponse> bookResponseList = buildListBooksResponseForBookPage(bookPage.getContent());
        paginationResponse.setResult(bookResponseList);
        return paginationResponse;
    }

    @Override
    public PaginationResponse fetchListBooksByMinRatingGreaterThanEqual(
            Specification<Book> spec,
            Double minRating,
            Pageable pageable
    ) {
        Specification<Book> minRatingSpec = BookSpecification.hasMinAverageRating(minRating)
                .and(BookSpecification.orderByAverageRatingDesc());
        Specification<Book> combinedSpec = spec != null
                ? spec.and(minRatingSpec)
                : minRatingSpec;

        Page<Book> bookPageWithAverageRating = bookRepository.findAll(combinedSpec, pageable);
        PaginationResponse paginationResponse
                = PaginationUtil.fetchAllDataWithPagination(bookPageWithAverageRating, pageable);

        List<BookResponse> bookResponseList = buildListBooksResponseForBookPage(bookPageWithAverageRating.getContent());
        paginationResponse.setResult(bookResponseList);
        return paginationResponse;
    }


    @Override
    public List<BookResponse> fetchAllBookResponsesFromBooks() {
        return buildListBooksResponseForBookPage(bookRepository.findAll());
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

        boolean shouldValidate = bookUpdated.getName() != null && bookUpdated.getAuthor() != null;
        if (shouldValidate) {
            validateBookForUpdate(bookGetById, bookUpdated);
        }

        if (StringUtils.hasValue(bookUpdated.getName())) {
            bookGetById.setName(StringUtils.normalizeSpaces(bookUpdated.getName()));
        }
        if (bookUpdated.getPrice() != null) {
            bookGetById.setPrice(bookUpdated.getPrice());
        }
        if (bookUpdated.getQuantity() != null) {
            bookGetById.setQuantity(bookUpdated.getQuantity());
        }
        if (StringUtils.hasValue(bookUpdated.getAuthor())) {
            bookGetById.setAuthor(StringUtils.normalizeSpaces(bookUpdated.getAuthor()));
        }
        if (StringUtils.hasValue(bookUpdated.getDescription())) {
            bookGetById.setDescription(StringUtils.trimBlank(bookUpdated.getDescription()));
        }
        if (StringUtils.hasValue(bookUpdated.getIntroduceText())) {
            bookGetById.setIntroduceText(StringUtils.trimBlank(bookUpdated.getIntroduceText()));
        }
        if (StringUtils.hasValue(bookUpdated.getReader())) {
            bookGetById.setReader(StringUtils.trimBlank(bookUpdated.getReader()));
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
                        messageTranslator.toLocale(BookEnum.ID_NOT_FOUND.getMessageKey(), id)
                ));
    }

    @Transactional
    @Override
    public void deleteBookById(Long id) {
        LOGGER.info("Deleting book id: {}", id);
        if (!checkIdExists(id)) {
            throw new NoSuchElementException(
                    messageTranslator.toLocale(BookEnum.ID_NOT_FOUND.getMessageKey(), id)
            );
        }
        // Don hang la du lieu lich su: xoa sach khong duoc lam bien mat dong hang da ban.
        if (orderDetailRepository.existsByBookId(id)) {
            throw new IllegalStateException(
                    messageTranslator.toLocale(BookEnum.REFERENCED_BY_ORDER.getMessageKey(), id)
            );
        }

        ratingRepository.deleteByBookId(id);
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
    public Set<String> findExistingNamesAndAuthorIgnoreCase(Set<String> keys) {
        return bookRepository.findExistingNamesAndAuthorsIgnoreCase(keys);
    }

    @Override
    public List<Book> getAllBooksByIds(Set<Long> bookIds) {
        return bookRepository.findAllByBookIds(bookIds);
    }

    @Transactional
    @Override
    public List<Book> saveAllBooks(List<Book> books) {
        return bookRepository.saveAll(books);
    }

    @Transactional
    @Override
    public ImportFileResponse<BookResponse> importBooksFromFile(List<BookCreateRequest> bookCreateRequestList) {

        ImportFileResponse<BookResponse> importFileResponse = new ImportFileResponse<>();
        List<BookResponse> successData = new ArrayList<>();
        List<ImportFileResponse.DetailFaultInfo<BookCreateRequest>> detailFaultInfoList = new ArrayList<>();
        List<Book> listBooksToSave = new ArrayList<>();
        Set<String> keysImported = new HashSet<>();

        int countSuccess = 0;
        int countError = 0;

        Set<String> existingNamesAndAuthorsIgnoreCase
                = getExistingNamesAndAuthorsIgnoreCaseFromBookCreateRequestList(bookCreateRequestList);

        Map<String, Category> mapCategories = getMapCategoriesWithStringFromBookCreateRequestList(bookCreateRequestList);

        for (int i = 0; i < bookCreateRequestList.size(); i++) {
            BookCreateRequest bookCreateRequest = bookCreateRequestList.get(i);

            try {
                String key = generateBookKeyNameMixAuthor(bookCreateRequest.getName(), bookCreateRequest.getAuthor());
                if (!keysImported.add(key)) {
                    throw new IllegalArgumentException(
                            messageTranslator.toLocale(
                                    BookEnum.NAME_AUTHOR_EXISTS.getMessageKey(),
                                    bookCreateRequest.getName(),
                                    bookCreateRequest.getAuthor()
                            )
                    );
                }

                validateBookForImport(existingNamesAndAuthorsIgnoreCase, bookCreateRequest);
                Book book = buildBookFromBookCreateRequest(bookCreateRequest);

                if (bookCreateRequest.getCategory() != null) {
                    Category category = mapCategories.get(bookCreateRequest.getCategory());
                    book.setCategory(category);
                }

                listBooksToSave.add(book);

                BookResponse bookResponse = bookMapper.toResponse(book, ActionConstant.CREATE);
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
                        bookCreateRequest
                ));

                countError++;

            } catch (Exception e) {
                detailFaultInfoList.add(new ImportFileResponse.DetailFaultInfo<>(
                        i + 1,
                        e.getMessage(),
                        bookCreateRequest
                ));

                countError++;
            }
        }

        if (!listBooksToSave.isEmpty()) {
            bookRepository.saveAll(listBooksToSave);
        }

        importFileResponse.setCountSuccess(countSuccess);
        importFileResponse.setCountError(countError);
        importFileResponse.setDetail(countError > 0
                ? detailFaultInfoList
                : messageTranslator.toLocale(CommonMessage.IMPORT_SUCCESS)
        );
        importFileResponse.setItems(successData);

        return importFileResponse;
    }

    private void validateBookForCreate(Book bookRequest) {
        if (checkNameAndAuthorExistsIgnoreCase(bookRequest.getName(), bookRequest.getAuthor())) {
            throw new IllegalArgumentException(
                    messageTranslator.toLocale(
                            BookEnum.NAME_AUTHOR_EXISTS.getMessageKey(),
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
                    messageTranslator.toLocale(
                            BookEnum.NAME_AUTHOR_EXISTS.getMessageKey(),
                            bookRequest.getName(),
                            bookRequest.getAuthor()
                    )
            );
        }
    }

    private void validateBookForImport(
            Set<String> existingNamesAndAuthorsIgnoreCase,
            BookCreateRequest bookCreateRequest
    ) {
        if (bookCreateRequest.getName() != null && bookCreateRequest.getAuthor() != null) {
            String keys = generateBookKeyNameMixAuthor(bookCreateRequest.getName(), bookCreateRequest.getAuthor());
            if (existingNamesAndAuthorsIgnoreCase.contains(keys)) {
                throw new IllegalArgumentException(
                        messageTranslator.toLocale(
                                BookEnum.NAME_AUTHOR_EXISTS.getMessageKey(),
                                bookCreateRequest.getName(),
                                bookCreateRequest.getAuthor()
                        )
                );
            }
        }
    }

    private Set<String> getExistingNamesAndAuthorsIgnoreCaseFromBookCreateRequestList(
            List<BookCreateRequest> bookCreateRequestList
    ) {
        Set<String> setKeysToCheck = bookCreateRequestList.stream()
                .filter(b -> (b.getName() != null && b.getAuthor() != null))
                .map(b -> generateBookKeyNameMixAuthor(b.getName(), b.getAuthor()))
                .collect(Collectors.toSet());

        return findExistingNamesAndAuthorIgnoreCase(setKeysToCheck);
    }

    private Map<String, Category> getMapCategoriesWithStringFromBookCreateRequestList(
            List<BookCreateRequest> bookCreateRequestList
    ) {
        Set<String> setNamesToCheck = bookCreateRequestList.stream()
                .map(BookCreateRequest::getCategory)
                .filter(Objects::nonNull)
                .map(StringUtils::convertValueTrim)
                .collect(Collectors.toSet());

        List<Category> categories = categoryService.findAllCategoriesByNamesIgnoreCase(setNamesToCheck);
        return categories.stream()
                .collect(Collectors.toMap(Category::getName, c -> c));
    }

    private Book buildBookFromBookCreateRequest(BookCreateRequest bookCreateRequest) {
        Book book = new Book();
        book.setName(bookCreateRequest.getName());
        book.setIntroduceText(bookCreateRequest.getIntroduceText());
        book.setDescription(bookCreateRequest.getDescription());
        book.setReader(bookCreateRequest.getReader());
        book.setPrice(bookCreateRequest.getPrice());
        book.setQuantity(bookCreateRequest.getQuantity());
        book.setAuthor(bookCreateRequest.getAuthor());
        book.setSold(bookCreateRequest.getSold());
        return book;
    }

    @Override
    public BookResponse getBookResponseById(Long id) {
        return bookMapper.toResponse(getBookById(id), ActionConstant.FETCH);
    }

    /** Diem trung binh nam san tren entity nen khong con phai ban them 1 cau query cho moi trang. */
    private List<BookResponse> buildListBooksResponseForBookPage(List<Book> allBooks) {
        return allBooks.stream()
                .map(book -> bookMapper.toResponse(book, ActionConstant.FETCH))
                .toList();
    }

    private String generateBookKeyNameMixAuthor(String name, String author) {
        String bookNameLowerCase = StringUtils.convertValueTrimLower(name);
        String bookAuthorLowerCase = StringUtils.convertValueTrimLower(author);
        return bookNameLowerCase + SymbolConstant.UNDERSCORE + bookAuthorLowerCase;
    }

    private void validateAndSetCategory(Book currentBook, Book bookRequest) {
        if (bookRequest.getCategory() != null) {
            Long categoryId = bookRequest.getCategory().getId();
            if (!categoryService.checkIdExists(categoryId)) {
                throw new NoSuchElementException(
                        messageTranslator.toLocale(CategoryEnum.ID_NOT_FOUND.getMessageKey(), categoryId)
                );
            }
            Category categoryGetById = categoryService.getCategoryById(categoryId);
            currentBook.setCategory(categoryGetById);
        }
    }
}
