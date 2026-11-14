package com.fullstack.bookShop.service.impl.imports;

import com.fullstack.bookShop.dto.request.imports.CategoryImportRequest;
import com.fullstack.bookShop.dto.response.CategoryResponse;
import com.fullstack.bookShop.mapper.CategoryMapper;
import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.repository.BookRepository;
import com.fullstack.bookShop.repository.CategoryRepository;
import com.fullstack.bookShop.util.batch.ImportExecutor;
import com.fullstack.bookShop.util.batch.ImportRow;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import com.fullstack.bookShop.util.enums.CategoryEnum;
import com.fullstack.bookShop.util.enums.ImportActionEnum;
import com.fullstack.bookShop.util.message.MessageTranslator;
import com.fullstack.bookShop.util.string.StringUtils;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryImportServiceImpl
        extends AbstractEntityImportService<CategoryImportRequest, Category, CategoryResponse, Set<String>> {

    private final CategoryMapper categoryMapper;

    private final CategoryRepository categoryRepository;

    private final BookRepository bookRepository;

    public CategoryImportServiceImpl(
            Validator validator,
            ImportExecutor importExecutor,
            MessageTranslator messageTranslator,
            CategoryMapper categoryMapper,
            CategoryRepository categoryRepository,
            BookRepository bookRepository
    ) {
        super(validator, importExecutor, messageTranslator);
        this.categoryMapper = categoryMapper;
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
    }

    /** Tap ten da ton tai, nap bang DUNG mot cau query thay vi hoi DB tung dong. */
    @Override
    protected Set<String> buildContext(List<ImportRow<CategoryImportRequest>> rows) {
        Set<String> names = rows.stream()
                .map(row -> businessKeyOf(row.source()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return names.isEmpty() ? Set.of() : categoryRepository.findExistingNamesIgnoreCase(names);
    }

    @Override
    protected String businessKeyOf(CategoryImportRequest request) {
        return StringUtils.trimLowerBlank(request.getName());
    }

    @Override
    protected ImportActionEnum actionOf(CategoryImportRequest request) {
        return request.getAction();
    }

    @Override
    protected Optional<Category> findExistingEntity(CategoryImportRequest request) {
        return categoryRepository.findByNameIgnoreCase(request.getName());
    }

    @Override
    protected String notFoundMessage(CategoryImportRequest request) {
        return messageTranslator.toLocale(CategoryEnum.NAME_NOT_FOUND.getMessageKey(), request.getName());
    }

    @Override
    protected Category createEntity(CategoryImportRequest request, Set<String> existingNames) {
        if (existingNames.contains(businessKeyOf(request))) {
            throw new IllegalArgumentException(messageTranslator.toLocale(
                    CategoryEnum.NAME_EXISTS.getMessageKey(), request.getName()));
        }

        Category category = new Category();
        category.setName(StringUtils.normalizeSpaces(request.getName()));
        return categoryRepository.save(category);
    }

    /**
     * The loai chi co mot truong name, ma name lai chinh la khoa dinh danh cua dong import.
     * Nen UPDATE khong con gi de doi: tra ve ban ghi hien co, coi nhu da dong bo.
     */
    @Override
    protected Category updateEntity(Category existing, CategoryImportRequest request, Set<String> existingNames) {
        return existing;
    }

    @Override
    protected void deleteEntity(Category existing) {
        // Giu nguyen luat cua luong xoa the loai dang co: xoa the loai keo theo sach thuoc the loai do
        bookRepository.deleteByCategoryId(existing.getId());
        categoryRepository.delete(existing);
    }

    @Override
    protected CategoryResponse toResponse(Category entity, ImportActionEnum action) {
        return categoryMapper.toResponse(entity, switch (action) {
            case CREATE -> ActionConstant.CREATE;
            case UPDATE -> ActionConstant.UPDATE;
            case DELETE -> ActionConstant.DELETE;
        });
    }
}
