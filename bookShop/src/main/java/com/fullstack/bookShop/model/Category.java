package com.fullstack.bookShop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fullstack.bookShop.util.constant.auth.AuthorityConstant;
import com.fullstack.bookShop.util.constant.entity.CategoryConstant;
import com.fullstack.bookShop.util.security.SecurityUtil;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = CategoryConstant.NAME_REQUIRED)
    @Size(max = 100, message = CategoryConstant.NAME_MAX_LENGTH)
    @Pattern(
            regexp = "^[A-Za-zÀ-ỹà-ỹ0-9'\\-\\.\\:\\s]{1,100}$",
            message = CategoryConstant.NAME_INVALID
    )
    private String name;

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Book> books;

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
