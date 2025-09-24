package com.fullstack.bookShop.domain.entity;

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
@AllArgsConstructor
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Book's name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    @Pattern(
            regexp = "^[A-Za-zÀ-ỹà-ỹ0-9'\\-\\.\\:\\s]{1,100}$",
            message = "Name must only contain letters, numbers, spaces, apostrophes, dashes, dots or colons"
    )
    private String name;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must be less than 100 characters")
    @Pattern(
            regexp = "^[A-Za-zÀ-ỹà-ỹ'\\-\\.\\s]{1,100}$",
            message = "Category must only contain letters, spaces, apostrophes, dashes or dots"
    )
    private String category;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be a positive number")
    private Double price;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 1000, message = "Quantity must not exceed 1000")
    private Integer quantity;

    private List<String> images;

    @NotBlank(message = "Author is required")
    @Size(max = 100, message = "Author must be less than 100 characters")
    @Pattern(
            regexp = "^[A-Za-zÀ-ỹà-ỹ'\\-\\.\\s]{1,100}$",
            message = "Author must only contain letters, spaces, apostrophes, dashes or dots"
    )
    private String author;

    private String sold;

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLoginEmail().orElse("");
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLoginEmail().orElse("");
    }
}
