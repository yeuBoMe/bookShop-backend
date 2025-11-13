package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.dto.category.RestCategoryCreateDTO;
import com.fullstack.bookShop.dto.category.RestCategoryUpdateDTO;
import com.fullstack.bookShop.dto.category.RestCategoryViewDTO;
import com.fullstack.bookShop.dto.imports.RestImportDTO;
import com.fullstack.bookShop.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.service.CategoryService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.imports.HttpStatusUtil;
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

    private final CategoryService categoryService;

    @ApiMessage("Create category")
    @PostMapping
    public ResponseEntity<RestCategoryCreateDTO> createCategoryRequest(@Valid @RequestBody Category category) {
        Category categoryCreated = categoryService.createCategory(category);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.convertCategoryToRestCategoryCreateDTO(categoryCreated));
    }

    @ApiMessage("Fetch list categories")
    @GetMapping
    public ResponseEntity<ResultPaginationDTO> fetchAllCategoriesRequest(
            @Filter Specification<Category> spec,
            Pageable pageable
    ) {
        return ResponseEntity.ok(categoryService.fetchAllCategories(spec, pageable));
    }

    @ApiMessage("Get category by id")
    @GetMapping("/{id}")
    public ResponseEntity<RestCategoryViewDTO> getCategoryByIdRequest(@PathVariable Long id) {
        Category categoryGetById = categoryService.getCategoryById(id);
        return ResponseEntity.ok(categoryService.convertCategoryToRestCategoryViewDTO(categoryGetById));
    }

    @ApiMessage("Update category by id")
    @PutMapping("/{id}")
    public ResponseEntity<RestCategoryUpdateDTO> updateCategoryByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody Category category
    ) {
        Category categoryUpdated = categoryService.updateCategoryById(id, category);
        return ResponseEntity.ok(categoryService.convertCategoryToRestCategoryUpdateDTO(categoryUpdated));
    }

    @ApiMessage("Update category partial by id")
    @PatchMapping("/{id}")
    public ResponseEntity<RestCategoryUpdateDTO> updateCategoryPartialByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody Category category
    ) {
        Category categoryUpdated = categoryService.updateCategoryPartialById(id, category);
        return ResponseEntity.ok(categoryService.convertCategoryToRestCategoryUpdateDTO(categoryUpdated));
    }

    @ApiMessage("Delete category")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoryByIdRequest(@PathVariable Long id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.noContent().build();
    }

    @ApiMessage("Import list categories from file")
    @PostMapping("/import")
    public ResponseEntity<RestImportDTO<RestCategoryCreateDTO>> importCategoriesFormFileRequest(
            @Valid @RequestBody List<RestCategoryCreateDTO> restCategoryCreateDTOList
    ) {
        RestImportDTO<RestCategoryCreateDTO> restImportDTO
                = categoryService.importListCategoriesFromFile(restCategoryCreateDTOList);

        HttpStatus httpStatus = HttpStatusUtil.determineHttpStatus(restImportDTO);

        return ResponseEntity
                .status(httpStatus)
                .body(restImportDTO);
    }
}
