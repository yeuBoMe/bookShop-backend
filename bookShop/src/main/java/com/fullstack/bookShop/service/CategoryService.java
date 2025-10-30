package com.fullstack.bookShop.service;

import com.fullstack.bookShop.domain.dto.category.RestCategoryCreateDTO;
import com.fullstack.bookShop.domain.dto.category.RestCategoryUpdateDTO;
import com.fullstack.bookShop.domain.dto.category.RestCategoryViewDTO;
import com.fullstack.bookShop.domain.dto.imports.RestImportDTO;
import com.fullstack.bookShop.domain.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.domain.entity.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface CategoryService {
    Category createCategory(Category category);
    Category updateCategoryById(Long id, Category category);
    Category updateCategoryPartialById(Long id, Category category);
    Category getCategoryById(Long id);

    void deleteCategoryById(Long id);

    boolean checkIdExists(Long id);
    boolean checkNameExistsIgnoreCase(String name);

    RestCategoryCreateDTO convertCategoryToRestCategoryCreateDTO(Category category);
    RestCategoryUpdateDTO convertCategoryToRestCategoryUpdateDTO(Category category);
    RestCategoryViewDTO convertCategoryToRestCategoryViewDTO(Category category);

    ResultPaginationDTO fetchAllCategories(Specification<Category> spec, Pageable pageable);

    RestImportDTO<RestCategoryCreateDTO> importListCategoriesFromFile(List<RestCategoryCreateDTO> restCategoryCreateDTOList);
}
