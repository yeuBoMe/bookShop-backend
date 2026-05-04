package com.fullstack.bookShop.dto.request;

import com.fullstack.bookShop.util.constant.entity.RoleConstant;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleCreateRequest {

    @NotBlank(message = RoleConstant.NAME_REQUIRED)
    @Size(max = 100, message = RoleConstant.NAME_MAX_LENGTH)
    private String name;

    @NotBlank(message = RoleConstant.DESCRIPTION_REQUIRED)
    @Size(max = 5000, message = RoleConstant.DESCRIPTION_MAX_LENGTH)
    private String description;

    @NotNull(message = RoleConstant.ACTIVE_REQUIRED)
    private Boolean active;
}