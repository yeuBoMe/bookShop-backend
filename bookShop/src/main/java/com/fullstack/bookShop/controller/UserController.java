package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.domain.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.domain.dto.user.RestListUsersImportDTO;
import com.fullstack.bookShop.domain.dto.user.RestUserCreateDTO;
import com.fullstack.bookShop.domain.dto.user.RestUserUpdateDTO;
import com.fullstack.bookShop.domain.dto.user.RestUserViewDTO;
import com.fullstack.bookShop.domain.entity.User;
import com.fullstack.bookShop.service.UserService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.exception.custom.StorageException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ApiMessage("Create user")
    public ResponseEntity<RestUserCreateDTO> createUserRequest(@Valid @RequestBody User userRequest) {
        User userCreated = this.userService.createUser(userRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.userService.convertUserToRestUserCreateDTO(userCreated));
    }

    @GetMapping
    @ApiMessage("Fetch list users")
    public ResponseEntity<ResultPaginationDTO> fetchAllUsersRequest(
            @Filter Specification<User> spec,
            Pageable pageable
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.userService.fetchAllUsers(spec, pageable));
    }

    @GetMapping("/{id}")
    @ApiMessage("Get user")
    public ResponseEntity<RestUserViewDTO> getUserByIdRequest(@PathVariable Long id) {
        User userGetById = this.userService.getUserById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.userService.convertUserToRestUserViewDTO(userGetById));
    }

    @PutMapping("/{id}")
    @ApiMessage("Update user")
    public ResponseEntity<RestUserUpdateDTO> updateUserByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody User userRequest
    ) {
        User userUpdated = this.userService.updateUserById(id, userRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.userService.convertUserToRestUserUpdateDTO(userUpdated));
    }

    @PatchMapping("/{id}")
    @ApiMessage("Update user partial")
    public ResponseEntity<RestUserUpdateDTO> updateUserPartialByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody User userRequest
    ) {
        User userUpdated = this.userService.updateUserPartialById(id, userRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.userService.convertUserToRestUserUpdateDTO(userUpdated));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete user")
    public ResponseEntity<Void> deleteUserByIdRequest(@PathVariable Long id) {
        this.userService.deleteUserById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(null);
    }

    @ApiMessage("Update user avatar")
    @PatchMapping("/{id}/avatar")
    public ResponseEntity<RestUserUpdateDTO> uploadUserAvatarRequest(
            @PathVariable Long id,
            @RequestParam("fileName") String fileName
    ) throws StorageException {
        if (fileName == null || fileName.isEmpty()) {
            throw new StorageException("File name cannot be empty");
        }

        User user = new User();
        user.setAvatar(fileName);
        User userUpdated = this.userService.updateUserPartialById(id, user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.userService.convertUserToRestUserUpdateDTO(userUpdated));
    }

    @ApiMessage("Import file list users")
    @PostMapping("/import")
    public ResponseEntity<RestListUsersImportDTO> importUsersFromFileRequest(
            @Valid @RequestBody List<RestUserCreateDTO> restUserCreateDTOList
    ) {
        RestListUsersImportDTO restListUsersImportDTO =
                this.userService.importListUsersFromFileExcel(restUserCreateDTOList);

        HttpStatus httpStatus;
        if (restListUsersImportDTO.getCountSuccess() == 0) {
            httpStatus = HttpStatus.BAD_REQUEST;
        } else if (restListUsersImportDTO.getCountError() > 0) {
            httpStatus = HttpStatus.MULTI_STATUS;
        } else {
            httpStatus = HttpStatus.CREATED;
        }

        return ResponseEntity
                .status(httpStatus)
                .body(restListUsersImportDTO);
    }
}
