package com.fullstack.bookShop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fullstack.bookShop.util.constant.auth.AuthorityConstant;
import com.fullstack.bookShop.util.constant.entity.BookConstant;
import com.fullstack.bookShop.util.security.SecurityUtil;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Min(value = 1, message = BookConstant.QUANTITY_MIN)
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

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Rating> ratings;

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil
                .getCurrentUserLoginEmail()
                .orElse(AuthorityConstant.SYSTEM);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil
                .getCurrentUserLoginEmail()
                .orElse(AuthorityConstant.SYSTEM);
    }
}
