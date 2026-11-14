package com.fullstack.bookShop.model.base;

import com.fullstack.bookShop.util.constant.auth.AuthorityConstant;
import com.fullstack.bookShop.util.security.SecurityUtil;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Bon truong theo doi dau vet, truoc day duoc chep lai y het o 7 entity.
 *
 * <p>Giu nguyen co che cu (@PrePersist / @PreUpdate doc nguoi dung tu SecurityUtil)
 * thay vi doi sang Spring Data auditing: doi se can them @EnableJpaAuditing va mot
 * AuditorAware, tuc la doi hanh vi chu khong chi go trung lap.
 *
 * <p>Ten cot sinh ra khong doi (created_at, updated_at, created_by, updated_by)
 * nen khong can migration.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AuditableEntity {

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.createdBy = currentActor();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = currentActor();
    }

    /** Luong khong co nguoi dung dang nhap (cronjob, seed du lieu) thi ghi la SYSTEM. */
    private String currentActor() {
        return SecurityUtil.getCurrentUserLoginEmail().orElse(AuthorityConstant.SYSTEM);
    }
}
