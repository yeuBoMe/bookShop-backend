package com.fullstack.bookShop.service;

import com.fullstack.bookShop.dto.category.RestCategoryCreateDTO;
import com.fullstack.bookShop.dto.category.RestCategoryUpdateDTO;
import com.fullstack.bookShop.dto.category.RestCategoryViewDTO;
import com.fullstack.bookShop.dto.imports.RestImportDTO;
import com.fullstack.bookShop.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.model.Category;
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
