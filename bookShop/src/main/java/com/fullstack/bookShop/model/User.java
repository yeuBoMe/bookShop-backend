package com.fullstack.bookShop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fullstack.bookShop.util.constant.auth.AuthorityConstant;
import com.fullstack.bookShop.util.constant.entity.UserConstant;
import com.fullstack.bookShop.util.security.SecurityUtil;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = UserConstant.FULL_NAME_REQUIRED)
    @Size(max = 100, message = UserConstant.FULL_NAME_MAX_LENGTH)
    @Pattern(
            regexp = "^[A-Za-zÀ-ỹà-ỹ0-9'\\-\\.\\s()]+$",
            message = UserConstant.FULL_NAME_INVALID
    )
    private String fullName;

    @Email(message = UserConstant.EMAIL_INVALID)
    @Size(max = 150, message = UserConstant.EMAIL_MAX_LENGTH)
    @Column(unique = true)
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$",
            message = UserConstant.EMAIL_INVALID
    )
    private String email;

    @Size(min = 6, message = UserConstant.PASSWORD_MIN_LENGTH)
    private String password;

    @Pattern(
            regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$",
            message = UserConstant.PHONE_INVALID
    )
    @Column(unique = true, length = 15)
    private String phone;

    private String avatar;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Rating> ratings;

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
