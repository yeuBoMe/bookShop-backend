package com.fullstack.bookShop.model;

import com.fullstack.bookShop.util.constant.auth.AuthorityConstant;
import com.fullstack.bookShop.util.constant.entity.OrderConstant;
import com.fullstack.bookShop.util.enums.StatusEnum;
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
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = OrderConstant.NAME_REQUIRED)
    @Size(max = 100, message = OrderConstant.NAME_MAX_LENGTH)
    @Pattern(
            regexp = "^[A-Za-zÀ-ỹà-ỹ'\\-\\.\\s]+$",
            message = OrderConstant.NAME_INVALID
    )
    private String name;

    @Email(message = OrderConstant.EMAIL_INVALID)
    @Size(max = 150, message = OrderConstant.EMAIL_MAX_LENGTH)
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$",
            message = OrderConstant.EMAIL_INVALID
    )
    private String email;

    @NotBlank(message = OrderConstant.PHONE_REQUIRED)
    @Pattern(
            regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$",
            message = OrderConstant.PHONE_INVALID
    )
    private String phone;

    @NotBlank(message = OrderConstant.ADDRESS_REQUIRED)
    @Size(max = 255, message = OrderConstant.ADDRESS_MAX_LENGTH)
    @Pattern(
            regexp = "^[A-Za-zÀ-ỹà-ỹ0-9'\\-,\\./#:\\s]{5,255}$",
            message = OrderConstant.ADDRESS_INVALID
    )
    private String address;

    @NotBlank(message = OrderConstant.PAYMENT_METHOD_REQUIRED)
    private String paymentMethod;

    private Double totalPrice;

    @Enumerated(EnumType.STRING)
    @NotNull(message = OrderConstant.STATUS_REQUIRED)
    @Column(length = 20, nullable = false)
    private StatusEnum status;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
//    @JsonIgnore
    private List<OrderDetail> detail;

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
