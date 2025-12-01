package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.dto.permission.RestPermissionCreateDTO;
import com.fullstack.bookShop.dto.permission.RestPermissionUpdateDTO;
import com.fullstack.bookShop.dto.permission.RestPermissionViewDTO;
import com.fullstack.bookShop.model.Permission;
import com.fullstack.bookShop.service.PermissionService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
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

    private final PermissionService permissionService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ApiMessage("Create permission")
    public ResponseEntity<RestPermissionCreateDTO> createPermissionRequest(
            @Valid @RequestBody Permission permissionRequest
    ) {
        Permission permissionCreated = permissionService.createPermission(permissionRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(permissionService.convertPermissionToRestPermissionCreateDTO(permissionCreated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @ApiMessage("Fetch list permissions")
    public ResponseEntity<ResultPaginationDTO> fetchAllPermissionsRequest(
            @Filter Specification<Permission> spec,
            Pageable pageable
    ) {
        return ResponseEntity.ok(permissionService.fetchAllPermissions(spec, pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    @ApiMessage("Get permission")
    public ResponseEntity<RestPermissionViewDTO> getPermissionByIdRequest(@PathVariable Long id) {
        Permission permissionGetById = permissionService.getPermissionById(id);
        return ResponseEntity.ok(permissionService.convertPermissionToRestPermissionViewDTO(permissionGetById));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @ApiMessage("Update permission")
    public ResponseEntity<RestPermissionUpdateDTO> updatePermissionByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody Permission permissionRequest
    ) {
        Permission permissionUpdated = permissionService.updatePermission(id, permissionRequest);
        return ResponseEntity.ok(permissionService.convertPermissionToRestPermissionUpdateDTO(permissionUpdated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ApiMessage("Delete permission")
    public ResponseEntity<Void> deletePermissionByIdRequest(@PathVariable Long id) {
        permissionService.deletePermissionById(id);
        return ResponseEntity.noContent().build();
    }
}
