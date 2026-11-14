package com.fullstack.bookShop.model;

import com.fullstack.bookShop.model.base.AuditableEntity;
import com.fullstack.bookShop.util.constant.entity.OrderConstant;
import com.fullstack.bookShop.util.enums.StatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_created_at_id", columnList = "created_at, id"),
                @Index(name = "idx_orders_name", columnList = "name"),
                @Index(name = "idx_orders_email_created_at", columnList = "email, created_at"),
                @Index(name = "idx_orders_status_created_at", columnList = "status, created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_id_seq")
    @SequenceGenerator(name = "orders_id_seq", sequenceName = "orders_id_seq", allocationSize = 50)
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
}
