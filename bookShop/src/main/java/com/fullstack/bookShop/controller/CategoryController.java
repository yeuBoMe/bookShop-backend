package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.service.interfaces.EntityImportService;
import com.fullstack.bookShop.dto.request.imports.CategoryImportRequest;
import com.fullstack.bookShop.dto.request.CategoryCreateRequest;
import com.fullstack.bookShop.dto.response.CategoryResponse;
import com.fullstack.bookShop.dto.response.imports.ImportFileResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.mapper.CategoryMapper;
import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.service.interfaces.CategoryService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.annotation.IsAdmin;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import com.fullstack.bookShop.util.status.HttpStatusUtil;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryMapper categoryMapper;

    private final CategoryService categoryService;

    private final EntityImportService<CategoryImportRequest, CategoryResponse> categoryImportService;

    @GetMapping
    @ApiMessage(ActionConstant.CATEGORY_FETCH_ALL)
    public ResponseEntity<PaginationResponse> fetchAllCategoriesRequest(
            @Filter Specification<Category> spec,
            Pageable pageable
    ) {
        return ResponseEntity.ok(categoryService.fetchAllCategories(spec, pageable));
    }

    @IsAdmin
    @PostMapping
    @ApiMessage(ActionConstant.CATEGORY_CREATE)
    public ResponseEntity<CategoryResponse> createCategoryRequest(@Valid @RequestBody Category categoryRequest) {
        Category categoryCreated = categoryService.createCategory(categoryRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryMapper.toResponse(categoryCreated, ActionConstant.CREATE));
    }

    @IsAdmin
    @GetMapping("/{id}")
    @ApiMessage(ActionConstant.CATEGORY_GET)
    public ResponseEntity<CategoryResponse> getCategoryByIdRequest(@PathVariable Long id) {
        Category categoryGetById = categoryService.getCategoryById(id);
        return ResponseEntity.ok(categoryMapper.toResponse(categoryGetById, ActionConstant.FETCH));
    }

    @IsAdmin
    @PutMapping("/{id}")
    @ApiMessage(ActionConstant.CATEGORY_UPDATE)
    public ResponseEntity<CategoryResponse> updateCategoryByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody Category categoryRequest
    ) {
        Category categoryUpdated = categoryService.updateCategoryById(id, categoryRequest);
        return ResponseEntity.ok(categoryMapper.toResponse(categoryUpdated, ActionConstant.UPDATE));
    }

    @IsAdmin
    @PatchMapping("/{id}")
    @ApiMessage(ActionConstant.CATEGORY_UPDATE_PARTIAL)
    public ResponseEntity<CategoryResponse> partialUpdateCategoryByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody Category categoryRequest
    ) {
        Category categoryUpdated = categoryService.partialUpdateCategoryPartialById(id, categoryRequest);
        return ResponseEntity.ok(categoryMapper.toResponse(categoryUpdated, ActionConstant.UPDATE));
    }

    @IsAdmin
    @DeleteMapping("/{id}")
    @ApiMessage(ActionConstant.CATEGORY_DELETE)
    public ResponseEntity<Void> deleteCategoryByIdRequest(@PathVariable Long id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.noContent().build();
    }

    @IsAdmin
    @PostMapping("/import")
    @ApiMessage(ActionConstant.CATEGORY_IMPORT)
    public ResponseEntity<ImportFileResponse<CategoryResponse>> importCategoriesFromFileRequest(
            @Valid @RequestBody List<@Valid CategoryCreateRequest> categoryCreateRequestList
    ) {
        ImportFileResponse<CategoryResponse> importFileResponse
                = categoryService.importCategoriesFromFile(categoryCreateRequestList);
        HttpStatus httpStatus = HttpStatusUtil.determineHttpStatus(importFileResponse);
        return ResponseEntity
                .status(httpStatus)
                .body(importFileResponse);
    }
    /**
     * Import theo dong: moi dong mang thao tac rieng (CREATE/UPDATE/DELETE) va duoc tinh
     * thanh cong / that bai doc lap. KHONG dat @Valid tren tung phan tu: mot dong sai dinh dang
     * se lam ca file bi tu choi 400 truoc khi vao service.
     */
    @IsAdmin
    @PostMapping("/import/batch")
    @ApiMessage(ActionConstant.CATEGORY_IMPORT_BATCH)
    public ResponseEntity<ImportFileResponse<CategoryResponse>> importCategoriesBatchRequest(
            @RequestBody List<CategoryImportRequest> categoryImportRequestList
    ) {
        ImportFileResponse<CategoryResponse> importFileResponse =
                categoryImportService.importFromFile(categoryImportRequestList);
        return ResponseEntity
                .status(HttpStatusUtil.determineHttpStatus(importFileResponse))
                .body(importFileResponse);
    }
}
