package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.dto.request.RoleCreateRequest;
import com.fullstack.bookShop.dto.response.RoleResponse;
import com.fullstack.bookShop.dto.response.imports.ImportFileResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.mapper.RoleMapper;
import com.fullstack.bookShop.model.Role;
import com.fullstack.bookShop.service.interfaces.RoleService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.annotation.IsAdmin;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import com.fullstack.bookShop.util.status.HttpStatusUtil;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleMapper roleMapper;

    private final RoleService roleService;

    @IsAdmin
    @GetMapping
    @ApiMessage(ActionConstant.ROLE_FETCH_ALL)
    public ResponseEntity<PaginationResponse> fetchAllRolesRequest(
            @Filter Specification<Role> spec,
            Pageable pageable
    ) {
        return ResponseEntity.ok(roleService.fetchAllRoles(spec, pageable));
    }

    @IsAdmin
    @PostMapping
    @ApiMessage(ActionConstant.ROLE_CREATE)
    public ResponseEntity<RoleResponse> createRoleRequest(@Valid @RequestBody Role roleRequest) {
        Role roleCreated = roleService.createRole(roleRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(roleMapper.toRoleResponse(roleCreated, ActionConstant.CREATE));
    }

    @IsAdmin
    @GetMapping("/{id}")
    @ApiMessage(ActionConstant.ROLE_GET)
    public ResponseEntity<RoleResponse> fetchRoleRequest(@PathVariable Long id) {
        Role roleGetById = roleService.getRoleById(id);
        return ResponseEntity.ok(roleMapper.toRoleResponse(roleGetById, ActionConstant.FETCH));
    }

    @IsAdmin
    @PutMapping("/{id}")
    @ApiMessage(ActionConstant.ROLE_UPDATE)
    public ResponseEntity<RoleResponse> updateRoleRequest(
            @PathVariable Long id,
            @Valid @RequestBody Role roleRequest
    ) {
        Role roleUpdated = roleService.updateRoleById(id, roleRequest);
        return ResponseEntity.ok(roleMapper.toRoleResponse(roleUpdated, ActionConstant.UPDATE));
    }

    @IsAdmin
    @DeleteMapping("/{id}")
    @ApiMessage(ActionConstant.ROLE_DELETE)
    public ResponseEntity<Void> deleteRoleRequest(@PathVariable Long id) {
        roleService.deleteRoleById(id);
        return ResponseEntity.noContent().build();
    }

    @IsAdmin
    @PostMapping("/import")
    @ApiMessage(ActionConstant.ROLE_IMPORT)
    public ResponseEntity<ImportFileResponse<RoleResponse>> importRolesFromFileRequest(
            @Valid @RequestBody List<RoleCreateRequest> roleCreateRequestList
    ) {
        ImportFileResponse<RoleResponse> importFileResponse = roleService.importRolesFromFile(roleCreateRequestList);
        HttpStatus status = HttpStatusUtil.determineHttpStatus(importFileResponse);
        return ResponseEntity
                .status(status)
                .body(importFileResponse);
    }
}