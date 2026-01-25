package com.fullstack.bookShop.service.interfaces;

import com.fullstack.bookShop.dto.request.CategoryCreateRequest;
import com.fullstack.bookShop.dto.response.CategoryResponse;
import com.fullstack.bookShop.dto.response.imports.ImportFileResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.model.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface CategoryService {

    PaginationResponse fetchAllCategories(Specification<Category> spec, Pageable pageable);

    Category createCategory(Category categoryRequest);

    Category updateCategoryById(Long id, Category categoryRequest);

    Category partialUpdateCategoryPartialById(Long id, Category categoryRequest);

    Category getCategoryById(Long id);

    void deleteCategoryById(Long id);

    boolean checkIdExists(Long id);

    boolean checkNameExistsIgnoreCase(String name);

    ImportFileResponse<CategoryResponse> importCategoriesFromFile(List<CategoryCreateRequest> categoryCreateRequestList);
}
