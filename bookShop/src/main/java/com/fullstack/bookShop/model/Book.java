package com.fullstack.bookShop.model;

import com.fullstack.bookShop.model.base.AuditableEntity;
import com.fullstack.bookShop.util.string.StringUtils;
import com.fullstack.bookShop.util.constant.SymbolConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fullstack.bookShop.util.constant.entity.BookConstant;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.List;

@Entity
@Table(
        name = "books",
        indexes = {
                @Index(name = "idx_books_category_id", columnList = "category_id"),
                @Index(name = "idx_books_name", columnList = "name"),
                @Index(name = "idx_books_author", columnList = "author"),

                // Keyset so sanh bo doi (gia_tri, id) nen index PHAI co ca hai cot,
                // theo dung thu tu voi tung kieu sap xep cua BookSortEnum.
                @Index(name = "idx_books_created_at_id", columnList = "created_at, id"),
                @Index(name = "idx_books_price_id", columnList = "price, id"),
                @Index(name = "idx_books_average_stars_id", columnList = "average_stars, id"),

                // Man hinh khach thuong loc the loai roi moi sap xep, nen can index gop
                @Index(name = "idx_books_category_created_at_id", columnList = "category_id, created_at, id"),
                @Index(name = "idx_books_category_price_id", columnList = "category_id, price, id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "books_id_seq")
    @SequenceGenerator(name = "books_id_seq", sequenceName = "books_id_seq", allocationSize = 50)
    private Long id;

    @NotBlank(message = BookConstant.NAME_REQUIRED)
    @Size(max = 100, message = BookConstant.NAME_MAX_LENGTH)
    @Pattern(
            regexp = "^[A-Za-zÀ-ỹà-ỹ0-9'\\-\\.\\:\\s]{1,100}$",
            message = BookConstant.NAME_INVALID
    )
    private String name;

    @NotBlank(message = BookConstant.INTRODUCE_TEXT_REQUIRED)
    @Size(max = 200, message = BookConstant.INTRODUCE_TEXT_MAX_LENGTH)
    @Column(columnDefinition = "TEXT")
    private String introduceText;

    @NotBlank(message = BookConstant.DESCRIPTION_REQUIRED)
    @Size(max = 5000, message = BookConstant.DESCRIPTION_MAX_LENGTH)
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = BookConstant.READER_REQUIRED)
    @Size(max = 200, message = BookConstant.READER_MAX_LENGTH)
    @Column(columnDefinition = "TEXT")
    private String reader;

    @NotNull(message = BookConstant.PRICE_REQUIRED)
    @DecimalMin(value = "0.0", message = BookConstant.PRICE_POSITIVE)
    private Double price;

    @NotNull(message = BookConstant.QUANTITY_REQUIRED)
    @Min(value = 0, message = BookConstant.QUANTITY_MIN)
    @Max(value = 1000, message = BookConstant.QUANTITY_MAX)
    private Integer quantity;

    private List<String> images;

    @NotBlank(message = BookConstant.AUTHOR_REQUIRED)
    @Size(max = 100, message = BookConstant.AUTHOR_MAX_LENGTH)
    @Pattern(
            regexp = "^[A-Za-zÀ-ỹà-ỹ'\\-\\.\\s]{1,100}$",
            message = BookConstant.AUTHOR_INVALID
    )
    private String author;

    private String sold;

    /**
     * Diem trung binh tinh san, cap nhat moi khi co danh gia moi hoac bi xoa.
     * Truoc day loc theo diem phai chay subquery tuong quan cho tung dong nen rat cham o 5K sach.
     */
    @ColumnDefault("0")
    @Column(nullable = false)
    private Double averageStars = 0.0;

    @ColumnDefault("0")
    @Column(nullable = false)
    private Integer ratingCount = 0;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Rating> ratings;

    /**
     * Ten + tac gia da chuan hoa (thuong hoa, bo dau) de o tim kiem khong phu thuoc dau tieng Viet.
     *
     * <p>Tinh san trong Java thay vi dung tsvector cua Postgres: cach nay chay giong nhau tren moi
     * he quan tri nen viet duoc test that, va tren Postgres van duoc index trigram tang toc.
     */
    @JsonIgnore
    @Column(length = 250)
    private String searchKey;

    @PrePersist
    @PreUpdate
    protected void refreshSearchKey() {
        this.searchKey = StringUtils.toSearchKey(name + SymbolConstant.SPACE + author);
    }
}
