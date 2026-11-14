package com.fullstack.bookShop.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Tham so tim kiem sach. Moi truong deu tuy chon, de trong thi khong loc theo truong do. */
@Getter
@Setter
@NoArgsConstructor
public class BookSearchRequest {

    /** Tu khoa, so khop khong phan biet hoa thuong va khong phan biet dau tieng Viet. */
    private String keyword;

    /** Loc nhieu the loai cung luc: man hinh khach cho tich nhieu o. */
    private List<Long> categoryIds;

    /** Loc theo TEN the loai: bang quan tri loc bang ten chu khong co san id. */
    private String categoryName;

    private Double minPrice;

    private Double maxPrice;

    private Double minRating;

    /** NEWEST (mac dinh) / PRICE_ASC / PRICE_DESC / RATING_DESC. Gia tri la se quay ve mac dinh. */
    private String sort;

    /** Con tro cua trang truoc. De trong nghia la lay trang dau. */
    private String cursor;

    private Integer size;
}
