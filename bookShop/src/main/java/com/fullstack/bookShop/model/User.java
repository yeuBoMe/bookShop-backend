package com.fullstack.bookShop.model;

import com.fullstack.bookShop.model.base.AuditableEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fullstack.bookShop.util.constant.entity.UserConstant;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_role_id", columnList = "role_id"),
                @Index(name = "idx_users_full_name", columnList = "full_name"),
                @Index(name = "idx_users_created_at_id", columnList = "created_at, id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_id_seq")
    @SequenceGenerator(name = "users_id_seq", sequenceName = "users_id_seq", allocationSize = 50)
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

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Rating> ratings;
}
