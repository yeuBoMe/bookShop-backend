package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.domain.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.domain.dto.permission.RestPermissionCreateDTO;
import com.fullstack.bookShop.domain.dto.permission.RestPermissionUpdateDTO;
import com.fullstack.bookShop.domain.dto.permission.RestPermissionViewDTO;
import com.fullstack.bookShop.domain.entity.Permission;
import com.fullstack.bookShop.service.PermissionService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    @ApiMessage("Create permission")
    public ResponseEntity<RestPermissionCreateDTO> createPermissionRequest(@Valid @RequestBody Permission permissionRequest) {
        Permission permissionCreated = this.permissionService.createPermission(permissionRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.permissionService.convertPermissionToRestPermissionCreateDTO(permissionCreated));
    }

    @GetMapping
    @ApiMessage("Fetch list permissions")
    public ResponseEntity<ResultPaginationDTO> fetchAllPermissionsRequest(
            @Filter Specification<Permission> spec,
            Pageable pageable
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.permissionService.fetchAllPermissions(spec, pageable));
    }

    @GetMapping("/{id}")
    @ApiMessage("Get permission")
    public ResponseEntity<RestPermissionViewDTO> getPermissionByIdRequest(@PathVariable Long id) {
        Permission permissionGetById = this.permissionService.getPermissionById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.permissionService.convertPermissionToRestPermissionViewDTO(permissionGetById));
    }

    @PutMapping("/{id}")
    @ApiMessage("Update permission")
    public ResponseEntity<RestPermissionUpdateDTO> updatePermissionByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody Permission permissionRequest
    ) {
        Permission permissionUpdated = this.permissionService.updatePermission(id, permissionRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.permissionService.convertPermissionToRestPermissionUpdateDTO(permissionUpdated));
    }


    @DeleteMapping("/{id}")
    @ApiMessage("Delete permission")
    public ResponseEntity<Void> deletePermissionByIdRequest(@PathVariable Long id) {
        this.permissionService.deletePermissionById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(null);
    }
}
