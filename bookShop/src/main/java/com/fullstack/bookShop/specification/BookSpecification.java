package com.fullstack.bookShop.specification;

import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.util.cursor.CursorUtil;
import com.fullstack.bookShop.util.enums.BookSortEnum;
import com.fullstack.bookShop.util.string.StringUtils;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;

/**
 * Cac dieu kien loc cho Book, tach khoi service de tai dung va doc duoc rieng.
 *
 * <p>Quy uoc: tham so null nghia la "khong loc theo tieu chi nay" nen tra ve dieu kien
 * luon dung. Nho vay noi chuoi .and(...) o service khong phai bao quanh boi if/else.
 */
public final class BookSpecification {

    private static final String FIELD_ID = "id";

    private static final String FIELD_PRICE = "price";

    private static final String FIELD_CREATED_AT = "createdAt";

    private static final String FIELD_SEARCH_KEY = "searchKey";

    private static final String FIELD_AVERAGE_STARS = "averageStars";

    private static final String FIELD_CATEGORY = "category";

    private static final String FIELD_NAME = "name";

    private BookSpecification() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    private static Specification<Book> alwaysTrue() {
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.isTrue(criteriaBuilder.literal(true));
    }

    /**
     * So khop khong phan biet hoa thuong va khong phan biet dau tieng Viet.
     */
    public static Specification<Book> hasKeyword(String keyword) {
        String normalized = StringUtils.toSearchKey(keyword);
        if (normalized == null) {
            return alwaysTrue();
        }

        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.like(root.get(FIELD_SEARCH_KEY), "%" + normalized + "%");
    }

    public static Specification<Book> hasCategoryIn(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return alwaysTrue();
        }

        return (root, criteriaQuery, criteriaBuilder) ->
                root.get(FIELD_CATEGORY).get(FIELD_ID).in(categoryIds);
    }

    /**
     * So khop chinh xac, khong phan biet hoa thuong.
     */
    public static Specification<Book> hasCategoryName(String categoryName) {
        String normalized = StringUtils.trimLowerBlank(categoryName);
        if (normalized == null) {
            return alwaysTrue();
        }

        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(criteriaBuilder.lower(
                        root.get(FIELD_CATEGORY).get(FIELD_NAME)), normalized
                );
    }

    public static Specification<Book> priceFrom(Double minPrice) {
        if (minPrice == null) {
            return alwaysTrue();
        }

        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get(FIELD_PRICE), minPrice);
    }

    public static Specification<Book> priceTo(Double maxPrice) {
        if (maxPrice == null) {
            return alwaysTrue();
        }

        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get(FIELD_PRICE), maxPrice);
    }

    /**
     * Loc theo diem trung binh da tinh san tren cot average_stars.
     * Truoc day cho nay chay subquery tuong quan cho tung dong nen rat cham o 5.000 sach.
     */
    public static Specification<Book> hasMinAverageRating(Double minRating) {
        if (minRating == null || minRating <= 0) {
            return alwaysTrue();
        }

        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get(FIELD_AVERAGE_STARS), minRating);
    }

    /**
     * Sap xep theo diem cao truoc khi client khong tu chon thu tu.
     * Chi ap cho truy van lay du lieu, khong ap cho cau dem cua Page.
     */
    public static Specification<Book> orderByAverageRatingDesc() {
        return (root, criteriaQuery, criteriaBuilder) -> {
            boolean isCountQuery = criteriaQuery != null
                    && (Long.class.equals(criteriaQuery.getResultType())
                    || long.class.equals(criteriaQuery.getResultType()));

            if (criteriaQuery != null && !isCountQuery) {
                criteriaQuery.orderBy(criteriaBuilder.desc(root.get(FIELD_AVERAGE_STARS)));
            }
            return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
        };
    }

    /**
     * Dieu kien "dung sau con tro": (gia tri vuot con tro) HOAC (bang con tro VA id vuot con tro).
     *
     * <p>Ve id la bat buoc: hai cuon cung gia (hoac cung diem, cung ngay tao) ma chi so sanh
     * gia tri thi trang sau se lap lai hoac bo sot dung nhung ban ghi bang nhau do.
     */
    public static Specification<Book> afterCursor(BookSortEnum sortMode, CursorUtil.Cursor cursor) {
        if (cursor == null) {
            return alwaysTrue();
        }

        return (root, criteriaQuery, criteriaBuilder) -> {
            Path<Long> idPath = root.get(FIELD_ID);

            if (sortMode == BookSortEnum.NEWEST) {
                Path<Instant> createdAtPath = root.get(FIELD_CREATED_AT);
                Instant boundary = cursor.asInstant();

                return criteriaBuilder.or(
                        criteriaBuilder.lessThan(createdAtPath, boundary),
                        criteriaBuilder.and(
                                criteriaBuilder.equal(createdAtPath, boundary),
                                criteriaBuilder.lessThan(idPath, cursor.id())
                        )
                );
            }

            Path<Double> sortPath = root.get(sortMode.getProperty());
            Double boundary = cursor.asDouble();

            Predicate sameValueThenId = criteriaBuilder.and(
                    criteriaBuilder.equal(sortPath, boundary),
                    sortMode.isAscending()
                            ? criteriaBuilder.greaterThan(idPath, cursor.id())
                            : criteriaBuilder.lessThan(idPath, cursor.id())
            );

            return criteriaBuilder.or(
                    sortMode.isAscending()
                            ? criteriaBuilder.greaterThan(sortPath, boundary)
                            : criteriaBuilder.lessThan(sortPath, boundary),
                    sameValueThenId
            );
        };
    }
}
