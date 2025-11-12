package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.category.RestCategoryCreateDTO;
import com.fullstack.bookShop.dto.category.RestCategoryUpdateDTO;
import com.fullstack.bookShop.dto.category.RestCategoryViewDTO;
import com.fullstack.bookShop.dto.imports.RestImportDTO;
import com.fullstack.bookShop.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.repository.BookRepository;
import com.fullstack.bookShop.repository.CategoryRepository;
import com.fullstack.bookShop.service.CategoryService;
import com.fullstack.bookShop.util.pagination.PageUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final BookRepository bookRepository;

    @Transactional
    @Override
    public Category createCategory(Category category) {
        if (checkNameExistsIgnoreCase(category.getName())) {
            throw new IllegalArgumentException("Category with name '" + category.getName() + "' already exists!");
        }
        return categoryRepository.save(category);
    }

    @Transactional
    @Override
    public Category updateCategoryById(Long id, Category categoryUpdated) {
        Category categoryGetById = getCategoryById(id);

        if (checkNameExistsIgnoreCase(categoryUpdated.getName())
                && !categoryGetById.getName().equals(categoryUpdated.getName())
        ) {
            throw new IllegalArgumentException(
                    "Category with name '" + categoryUpdated.getName() + "' already exists!"
            );
        }

        categoryGetById.setName(categoryUpdated.getName());
        return categoryRepository.save(categoryGetById);
    }

    @Transactional
    @Override
    public Category updateCategoryPartialById(Long id, Category categoryUpdated) {
        Category categoryGetById = getCategoryById(id);

        if (checkNameExistsIgnoreCase(categoryUpdated.getName())
                && !categoryGetById.getName().equals(categoryUpdated.getName())
        ) {
            throw new IllegalArgumentException(
                    "Category with name '" + categoryUpdated.getName() + "' already exists!"
            );
        }

        if (categoryUpdated.getName() != null && !categoryUpdated.getName().isEmpty()) {
            categoryGetById.setName(categoryUpdated.getName());
        }

        return categoryRepository.save(categoryGetById);
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException("Category with id '" + id + "' does not exist!"));
    }

    @Transactional
    @Override
    public void deleteCategoryById(Long id) {
        if (!checkIdExists(id)) {
            throw new NoSuchElementException("Category with id '" + id + "' does not exist!");
        }
        bookRepository.deleteByCategoryId(id);
        categoryRepository.deleteById(id);
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
    public RestCategoryCreateDTO convertCategoryToRestCategoryCreateDTO(Category category) {
        return new RestCategoryCreateDTO(
                category.getId(),
                category.getName(),
                category.getCreatedAt(),
                category.getCreatedBy()
        );
    }

    @Override
    public RestCategoryUpdateDTO convertCategoryToRestCategoryUpdateDTO(Category category) {
        return new RestCategoryUpdateDTO(
                category.getId(),
                category.getName(),
                category.getUpdatedAt(),
                category.getUpdatedBy()
        );
    }

    @Override
    public RestCategoryViewDTO convertCategoryToRestCategoryViewDTO(Category category) {
        return new RestCategoryViewDTO(
                category.getId(),
                category.getName(),
                category.getCreatedAt(),
                category.getUpdatedAt(),
                category.getCreatedBy(),
                category.getUpdatedBy()
        );
    }

    @Override
    public ResultPaginationDTO fetchAllCategories(Specification<Category> spec, Pageable pageable) {
        Page<Category> categoryPage = categoryRepository.findAll(spec, pageable);

        List<RestCategoryViewDTO> restCategoryViewDTOList = categoryPage.getContent()
                .stream()
                .map(this::convertCategoryToRestCategoryViewDTO)
                .toList();

        ResultPaginationDTO resultPaginationDTO = PageUtil.handleFetchAllDataWithPagination(categoryPage, pageable);
        resultPaginationDTO.setResult(restCategoryViewDTOList);

        return resultPaginationDTO;
    }

    @Override
    public RestImportDTO<RestCategoryCreateDTO> importListCategoriesFromFile(
            List<RestCategoryCreateDTO> restCategoryCreateDTOList
    ) {
        RestImportDTO<RestCategoryCreateDTO> restImportDTO = new RestImportDTO<>();
        List<RestCategoryCreateDTO> successData = new ArrayList<>();
        List<RestImportDTO.DetailFaultInfo<RestCategoryCreateDTO>> detailFaultInfoList = new ArrayList<>();

        int countSuccess = 0;
        int countError = 0;

        for (int i = 0; i < restCategoryCreateDTOList.size(); i++) {
            RestCategoryCreateDTO restCategoryCreateDTO = restCategoryCreateDTOList.get(i);

            try {
                if (checkNameExistsIgnoreCase(restCategoryCreateDTO.getName())) {
                    throw new IllegalArgumentException(
                            "Category with name '" + restCategoryCreateDTO.getName() + "' already exists"
                    );
                }

                Category category = buildCategoryFromRestCategoryCreateDTO(restCategoryCreateDTO);
                RestCategoryCreateDTO categoryCreateDTO = convertCategoryToRestCategoryCreateDTO(category);
                successData.add(categoryCreateDTO);
                countSuccess++;

            } catch (ConstraintViolationException e) {
                List<String> errorMessage = e.getConstraintViolations().stream()
                        .map(ConstraintViolation::getMessage)
                        .toList();
                detailFaultInfoList.add(new RestImportDTO.DetailFaultInfo<>(
                        i + 1,
                        errorMessage,
                        restCategoryCreateDTO
                ));
                countError++;

            } catch (Exception e) {
                detailFaultInfoList.add(new RestImportDTO.DetailFaultInfo<>(
                        i + 1,
                        e.getMessage(),
                        restCategoryCreateDTO
                ));
                countError++;
            }
        }

        restImportDTO.setCountSuccess(countSuccess);
        restImportDTO.setCountError(countError);
        restImportDTO.setItems(successData);
        restImportDTO.setDetail(countError > 0 ? detailFaultInfoList : "Import categories successfully!");

        return restImportDTO;
    }

    private Category buildCategoryFromRestCategoryCreateDTO(RestCategoryCreateDTO restCategoryCreateDTO) {
        Category category = new Category();
        category.setName(restCategoryCreateDTO.getName());
        return categoryRepository.save(category);
    }
}
