package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.domain.dto.category.RestCategoryCreateDTO;
import com.fullstack.bookShop.domain.dto.category.RestCategoryUpdateDTO;
import com.fullstack.bookShop.domain.dto.category.RestCategoryViewDTO;
import com.fullstack.bookShop.domain.dto.imports.RestImportDTO;
import com.fullstack.bookShop.domain.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.domain.entity.Category;
import com.fullstack.bookShop.service.CategoryService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.imports.HttpStatusUtil;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @ApiMessage("Create category")
    @PostMapping
    public ResponseEntity<RestCategoryCreateDTO> createCategoryRequest(@Valid @RequestBody Category category) {
        Category categoryCreated = this.categoryService.createCategory(category);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.categoryService.convertCategoryToRestCategoryCreateDTO(categoryCreated));
    }

    @ApiMessage("Fetch list categories")
    @GetMapping
    public ResponseEntity<ResultPaginationDTO> fetchAllCategoriesRequest(
            @Filter Specification<Category> spec,
            Pageable pageable
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.categoryService.fetchAllCategories(spec, pageable));
    }

    @ApiMessage("Get category by id")
    @GetMapping("/{id}")
    public ResponseEntity<RestCategoryViewDTO> getCategoryByIdRequest(@PathVariable Long id) {
        Category categoryGetById = this.categoryService.getCategoryById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.categoryService.convertCategoryToRestCategoryViewDTO(categoryGetById));
    }

    @ApiMessage("Update category by id")
    @PutMapping("/{id}")
    public ResponseEntity<RestCategoryUpdateDTO> updateCategoryByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody Category category
    ) {
        Category categoryUpdated = this.categoryService.updateCategoryById(id, category);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.categoryService.convertCategoryToRestCategoryUpdateDTO(categoryUpdated));
    }

    @ApiMessage("Update category partial by id")
    @PatchMapping("/{id}")
    public ResponseEntity<RestCategoryUpdateDTO> updateCategoryPartialByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody Category category
    ) {
        Category categoryUpdated = this.categoryService.updateCategoryPartialById(id, category);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.categoryService.convertCategoryToRestCategoryUpdateDTO(categoryUpdated));
    }

    @ApiMessage("Delete category")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoryByIdRequest(@PathVariable Long id) {
        this.categoryService.deleteCategoryById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(null);
    }

    @ApiMessage("Import list categories from file")
    @PostMapping("/import")
    public ResponseEntity<RestImportDTO<RestCategoryCreateDTO>> importCategoriesFormFileRequest(
            @Valid @RequestBody List<RestCategoryCreateDTO> restCategoryCreateDTOList
    ) {
        RestImportDTO<RestCategoryCreateDTO> restImportDTO
                = this.categoryService.importListCategoriesFromFile(restCategoryCreateDTOList);

        HttpStatus httpStatus = HttpStatusUtil.determineHttpStatus(restImportDTO);

        return ResponseEntity
                .status(httpStatus)
                .body(restImportDTO);
    }
}
