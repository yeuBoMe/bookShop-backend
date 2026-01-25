package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.dto.response.PermissionResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.mapper.PermissionMapper;
import com.fullstack.bookShop.model.Permission;
import com.fullstack.bookShop.service.interfaces.PermissionService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.constant.auth.AuthorityConstant;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionMapper permissionMapper;

    private final PermissionService permissionService;

    @PreAuthorize("hasRole('" + AuthorityConstant.ADMIN + "')")
    @GetMapping
    @ApiMessage(ActionConstant.PERMISSION_FETCH_ALL)
    public ResponseEntity<PaginationResponse> fetchAllPermissionsRequest(
            @Filter Specification<Permission> spec,
            Pageable pageable
    ) {
        return ResponseEntity.ok(permissionService.fetchAllPermissions(spec, pageable));
    }

    @PreAuthorize("hasRole('" + AuthorityConstant.ADMIN + "')")
    @PostMapping
    @ApiMessage(ActionConstant.PERMISSION_CREATE)
    public ResponseEntity<PermissionResponse> createPermissionRequest(
            @Valid @RequestBody Permission permissionRequest
    ) {
        Permission permissionCreated = permissionService.createPermission(permissionRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(permissionMapper.convertPermissionToCreateResponse(permissionCreated));
    }

    @PreAuthorize("hasRole('" + AuthorityConstant.ADMIN + "')")
    @GetMapping("/{id}")
    @ApiMessage(ActionConstant.PERMISSION_GET)
    public ResponseEntity<PermissionResponse> fetchPermissionRequest(@PathVariable Long id) {
        Permission permissionGetById = permissionService.getPermissionById(id);
        return ResponseEntity.ok(permissionMapper.convertPermissionToViewResponse(permissionGetById));
    }

    @PreAuthorize("hasRole('" + AuthorityConstant.ADMIN + "')")
    @PutMapping("/{id}")
    @ApiMessage(ActionConstant.PERMISSION_UPDATE)
    public ResponseEntity<PermissionResponse> updatePermissionRequest(
            @PathVariable Long id,
            @Valid @RequestBody Permission permissionRequest
    ) {
        Permission permissionUpdated = permissionService.updatePermissionById(id, permissionRequest);
        return ResponseEntity.ok(permissionMapper.convertPermissionToUpdateResponse(permissionUpdated));
    }

    @PreAuthorize("hasRole('" + AuthorityConstant.ADMIN + "')")
    @DeleteMapping("/{id}")
    @ApiMessage(ActionConstant.PERMISSION_DELETE)
    public ResponseEntity<PermissionResponse> deletePermissionRequest(@PathVariable Long id) {
        permissionService.deletePermissionById(id);
        return ResponseEntity.noContent().build();
    }
}
