package com.fullstack.bookShop.util.enums;

import com.fullstack.bookShop.util.string.StringUtils;
import org.springframework.data.domain.Sort;

/**
 * Cach sap xep cho endpoint tim kiem theo con tro.
 *
 * <p>Moi kieu deu phai co id lam khoa phu: hai cuon cung gia (hoac cung diem, cung ngay tao)
 * ma khong co khoa phu thi thu tu giua hai lan goi co the khac nhau, khien trang sau
 * lap lai hoac bo sot ban ghi.
 */
public enum BookSortEnum {

    NEWEST("createdAt", Sort.Direction.DESC),
    PRICE_ASC("price", Sort.Direction.ASC),
    PRICE_DESC("price", Sort.Direction.DESC),
    RATING_DESC("averageStars", Sort.Direction.DESC);

    private final String property;

    private final Sort.Direction direction;

    BookSortEnum(String property, Sort.Direction direction) {
        this.property = property;
        this.direction = direction;
    }

    public static BookSortEnum resolveOrDefault(String value) {
        String normalized = StringUtils.trimUpperBlank(value);
        if (normalized == null) {
            return NEWEST;
        }

        try {
            return BookSortEnum.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return NEWEST;
        }
    }

    public String getProperty() {
        return property;
    }

    public Sort.Direction getDirection() {
        return direction;
    }

    public boolean isAscending() {
        return direction == Sort.Direction.ASC;
    }

    public Sort toSort() {
        return Sort.by(direction, property).and(Sort.by(direction, "id"));
    }
}
