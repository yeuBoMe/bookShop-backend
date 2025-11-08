package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.domain.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.domain.dto.role.RestRoleCreateDTO;
import com.fullstack.bookShop.domain.dto.role.RestRoleUpdateDTO;
import com.fullstack.bookShop.domain.dto.role.RestRoleViewDTO;
import com.fullstack.bookShop.domain.entity.Role;
import com.fullstack.bookShop.service.RoleService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @ApiMessage("Create role")
    public ResponseEntity<RestRoleCreateDTO> createRoleRequest(@Valid @RequestBody Role roleRequest) {
        Role roleCreated = roleService.createRole(roleRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(roleService.convertRoleToRestRoleCreateDTO(roleCreated));
    }

    @GetMapping
    @ApiMessage("Fetch list roles")
    public ResponseEntity<ResultPaginationDTO> fetchAllRolesRequest(
            @Filter Specification<Role> spec,
            Pageable pageable
    ) {
        return ResponseEntity.ok(roleService.fetchAllRoles(spec, pageable));
    }

    @GetMapping("/{id}")
    @ApiMessage("Get role")
    public ResponseEntity<RestRoleViewDTO> getRoleByIdRequest(@PathVariable Long id) {
        Role roleGetById = roleService.getRoleById(id);
        return ResponseEntity.ok(roleService.convertRoleToRestRoleViewDTO(roleGetById));
    }

    @PutMapping("/{id}")
    @ApiMessage("Update role")
    public ResponseEntity<RestRoleUpdateDTO> updateRoleByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody Role roleRequest
    ) {
        Role roleUpdated = roleService.updateRoleById(id, roleRequest);
        return ResponseEntity.ok(roleService.convertRoleToRestRoleUpdateDTO(roleUpdated));
    }


    @DeleteMapping("/{id}")
    @ApiMessage("Delete role")
    public ResponseEntity<Void> deleteRoleByIdRequest(@PathVariable Long id) {
        roleService.deleteRoleById(id);
        return ResponseEntity.noContent().build();
    }
}
