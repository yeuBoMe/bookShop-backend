package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.request.CategoryCreateRequest;
import com.fullstack.bookShop.dto.response.CategoryResponse;
import com.fullstack.bookShop.dto.response.imports.ImportFileResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.mapper.CategoryMapper;
import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.repository.BookRepository;
import com.fullstack.bookShop.repository.CategoryRepository;
import com.fullstack.bookShop.service.interfaces.CategoryService;
import com.fullstack.bookShop.util.constant.CommonMessage;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import com.fullstack.bookShop.util.enums.CategoryEnum;
import com.fullstack.bookShop.util.pagination.PaginationUtil;
import com.fullstack.bookShop.util.message.MessageTranslator;
import com.fullstack.bookShop.util.string.StringUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final MessageTranslator messageTranslator;

    private final CategoryMapper categoryMapper;

    private final BookRepository bookRepository;

    private final CategoryRepository categoryRepository;

    @Override
    public PaginationResponse fetchAllCategories(Specification<Category> spec, Pageable pageable) {
        Page<Category> categoryPage = categoryRepository.findAll(spec, pageable);

        List<CategoryResponse> categoryResponseList = categoryPage.getContent()
                .stream()
                .map(category -> categoryMapper.toResponse(category, ActionConstant.FETCH))
                .toList();

        PaginationResponse paginationResponse = PaginationUtil.fetchAllDataWithPagination(categoryPage, pageable);
        paginationResponse.setResult(categoryResponseList);
        return paginationResponse;
    }

    @Transactional
    @Override
    public Category createCategory(Category categoryRequest) {
        LOGGER.info("Creating category name={}", categoryRequest.getName());

        validateCategoryForCreate(categoryRequest);
        Category categoryCreated = categoryRepository.save(categoryRequest);

        LOGGER.info("Create category successfully with id: {}", categoryCreated.getId());
        return categoryCreated;
    }

    @Transactional
    @Override
    public Category updateCategoryById(Long id, Category categoryRequest) {
        LOGGER.info("Updating category id: {}", id);

        Category categoryGetById = getCategoryById(id);
        validateCategoryForUpdate(categoryGetById, categoryRequest);
        categoryGetById.setName(categoryRequest.getName());

        LOGGER.info("Update category successfully with id: {}", id);
        return categoryRepository.save(categoryGetById);
    }

    @Transactional
    @Override
    public Category partialUpdateCategoryPartialById(Long id, Category categoryRequest) {
        LOGGER.info("Updating category partial id: {}", id);

        Category categoryGetById = getCategoryById(id);
        validateCategoryForUpdate(categoryGetById, categoryRequest);

        if (StringUtils.hasValue(categoryRequest.getName())) {
            categoryGetById.setName(categoryRequest.getName());
        }

        LOGGER.info("Update category partial successfully with id: {}", id);
        return categoryRepository.save(categoryGetById);
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        messageTranslator.toLocale(CategoryEnum.ID_NOT_FOUND.getMessageKey(), id)
                ));
    }

    @Transactional
    @Override
    public void deleteCategoryById(Long id) {
        LOGGER.info("Deleting category id: {}", id);
        if (!checkIdExists(id)) {
            throw new NoSuchElementException(
                    messageTranslator.toLocale(CategoryEnum.ID_NOT_FOUND.getMessageKey(), id)
            );
        }
        bookRepository.deleteByCategoryId(id);
        categoryRepository.deleteById(id);
        LOGGER.info("Delete category successfully with id: {}", id);
    }

    @Override
    public boolean checkIdExists(Long id) {
        return categoryRepository.existsById(id);
    }

    @Override
    public boolean checkNameExistsIgnoreCase(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public Set<String> findExistingNamesIgnoreCase(Set<String> names) {
        return categoryRepository.findExistingNamesIgnoreCase(names);
    }

    @Override
    public List<Category> findAllCategoriesByNamesIgnoreCase(Set<String> names) {
        return categoryRepository.findAllByNamesIgnoreCase(names);
    }

    @Override
    public ImportFileResponse<CategoryResponse> importCategoriesFromFile(
            List<CategoryCreateRequest> categoryCreateRequestList
    ) {
        ImportFileResponse<CategoryResponse> importFileResponse = new ImportFileResponse<>();
        List<CategoryResponse> successData = new ArrayList<>();
        List<ImportFileResponse.DetailFaultInfo<CategoryCreateRequest>> detailFaultInfoList = new ArrayList<>();
        List<Category> listCategoriesToSave = new ArrayList<>();

        int countSuccess = 0;
        int countError = 0;

        Set<String> existingNamesIgnoreCase = getExistingNamesIgnoreCaseFromCategoryCreateRequest(categoryCreateRequestList);

        for (int i = 0; i < categoryCreateRequestList.size(); i++) {
            CategoryCreateRequest categoryCreateRequest = categoryCreateRequestList.get(i);
            int index = i + 1;

            try {
                validateCategoryForImport(existingNamesIgnoreCase, categoryCreateRequest);

                Category category = buildCategoryFromRestCategoryCreateDTO(categoryCreateRequest);
                listCategoriesToSave.add(category);

                CategoryResponse categoryResponse = categoryMapper.toResponse(category, ActionConstant.CREATE);
                successData.add(categoryResponse);
                countSuccess++;

                LOGGER.info("Import category successfully with index {}: {}", index, categoryCreateRequest.getName());

            } catch (ConstraintViolationException e) {
                List<String> errorMessage = e.getConstraintViolations().stream()
                        .map(ConstraintViolation::getMessage)
                        .toList();

                detailFaultInfoList.add(new ImportFileResponse.DetailFaultInfo<>(
                        index,
                        errorMessage,
                        categoryCreateRequest
                ));

                countError++;
                LOGGER.warn("Import failed at row {} due to validation: {}", index, errorMessage);

            } catch (Exception ex) {
                detailFaultInfoList.add(new ImportFileResponse.DetailFaultInfo<>(
                        i + 1,
                        ex.getMessage(),
                        categoryCreateRequest
                ));

                countError++;
                LOGGER.error("Unexpected error importing category at row {}: {}", index, ex.getMessage(), ex);
            }
        }

        if (!listCategoriesToSave.isEmpty()) {
            categoryRepository.saveAll(listCategoriesToSave);
            LOGGER.info("List categories saved {} categories successfully", listCategoriesToSave.size());
        }

        importFileResponse.setCountSuccess(countSuccess);
        importFileResponse.setCountError(countError);
        importFileResponse.setItems(successData);
        importFileResponse.setDetail(countError > 0 ? detailFaultInfoList : messageTranslator.toLocale(CommonMessage.IMPORT_SUCCESS));

        return importFileResponse;
    }

    private Set<String> getExistingNamesIgnoreCaseFromCategoryCreateRequest(
            List<CategoryCreateRequest> categoryCreateRequestList
    ) {
        Set<String> setNamesToCheck = categoryCreateRequestList.stream()
                .map(CategoryCreateRequest::getName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toSet());
        return findExistingNamesIgnoreCase(setNamesToCheck);
    }

    private void validateCategoryForCreate(Category categoryRequest) {
        if (checkNameExistsIgnoreCase(categoryRequest.getName())) {
            LOGGER.warn("Create failed: Category name '{}' already exists", categoryRequest.getName());
            throw new IllegalArgumentException(
                    messageTranslator.toLocale(CategoryEnum.NAME_EXISTS.getMessageKey(), categoryRequest.getName())
            );
        }
    }

    private void validateCategoryForUpdate(Category currentCategory, Category categoryRequest) {
        boolean isNameExists = checkNameExistsIgnoreCase(categoryRequest.getName());
        if (isNameExists && !(currentCategory.getName().equals(categoryRequest.getName()))) {
            LOGGER.warn("Update failed: Category name '{}' already exists", categoryRequest.getName());
            throw new IllegalArgumentException(
                    messageTranslator.toLocale(CategoryEnum.NAME_EXISTS.getMessageKey(), categoryRequest.getName())
            );
        }
    }

    private void validateCategoryForImport(
            Set<String> existingNamesIgnoreCase,
            CategoryCreateRequest categoryCreateRequest
    ) {
        // Check name exists
        String normalizedNameLowerCase = StringUtils.trimLowerBlank(categoryCreateRequest.getName());

        if (existingNamesIgnoreCase.contains(normalizedNameLowerCase)) {
            LOGGER.warn("Import failed: Category name '{}' already exists", categoryCreateRequest.getName());
            throw new IllegalArgumentException(
                    messageTranslator.toLocale(CategoryEnum.NAME_EXISTS.getMessageKey(), categoryCreateRequest.getName())
            );
        }
    }

    private Category buildCategoryFromRestCategoryCreateDTO(CategoryCreateRequest categoryCreateRequest) {
        Category category = new Category();
        category.setName(categoryCreateRequest.getName());
        return category;
    }
}
