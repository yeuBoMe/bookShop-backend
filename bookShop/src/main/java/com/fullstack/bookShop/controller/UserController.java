package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.dto.request.user.UserCreateRequest;
import com.fullstack.bookShop.dto.request.user.UserPasswordRequest;
import com.fullstack.bookShop.dto.response.UserResponse;
import com.fullstack.bookShop.dto.response.imports.ImportFileResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.mapper.UserMapper;
import com.fullstack.bookShop.model.User;
import com.fullstack.bookShop.service.interfaces.UserService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.exception.custom.FileException;
import com.fullstack.bookShop.util.annotation.HasAdminOrSelf;
import com.fullstack.bookShop.util.constant.auth.AuthorityConstant;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import com.fullstack.bookShop.util.status.HttpStatusUtil;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;

    private final UserService userService;

    @HasAdminOrSelf
    @GetMapping
    @ApiMessage(ActionConstant.USER_FETCH_ALL)
    public ResponseEntity<PaginationResponse> fetchAllUsersRequest(
            @Filter Specification<User> spec,
            Pageable pageable
    ) {
        return ResponseEntity.ok(userService.fetchAllUsers(spec, pageable));
    }

    @HasAdminOrSelf
    @PostMapping
    @ApiMessage(ActionConstant.USER_CREATE)
    public ResponseEntity<UserResponse> createUserRequest(@Valid @RequestBody User userRequest) {
        User userCreated = userService.createUser(userRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userMapper.toResponse(userCreated, ActionConstant.CREATE));
    }

    @HasAdminOrSelf
    @GetMapping("/{id}")
    @ApiMessage(ActionConstant.USER_GET)
    public ResponseEntity<UserResponse> fetchUserById(@PathVariable Long id) {
        User userGetById = userService.getUserById(id);
        return ResponseEntity.ok(userMapper.toResponse(userGetById, ActionConstant.FETCH));
    }

    @HasAdminOrSelf
    @PutMapping("/{id}")
    @ApiMessage(ActionConstant.USER_UPDATE)
    public ResponseEntity<UserResponse> updateUserRequest(
            @PathVariable Long id,
            @Valid @RequestBody User userRequest
    ) {
        User userUpdated = userService.updateUserById(id, userRequest);
        return ResponseEntity.ok(userMapper.toResponse(userUpdated, ActionConstant.UPDATE));
    }

    @HasAdminOrSelf
    @PatchMapping("/{id}")
    @ApiMessage(ActionConstant.USER_UPDATE_PARTIAL)
    public ResponseEntity<UserResponse> partialUpdateUserRequest(
            @PathVariable Long id,
            @Valid @RequestBody User userRequest
    ) {
        User userPartialUpdated = userService.partialUpdateUserById(id, userRequest);
        return ResponseEntity.ok(userMapper.toResponse(userPartialUpdated, ActionConstant.UPDATE));
    }

    @PreAuthorize("#id == authentication.principal.claims['user']['id']")
    @PatchMapping("/{id}/password")
    @ApiMessage(ActionConstant.USER_UPDATE_PASSWORD)
    public ResponseEntity<UserResponse> updateUserPasswordRequest(
            @PathVariable Long id,
            @Valid @RequestBody UserPasswordRequest userPasswordRequest
    ) {
        User userPasswordUpdated = userService.updateUserPasswordById(id, userPasswordRequest);
        return ResponseEntity.ok(userMapper.toResponse(userPasswordUpdated, ActionConstant.UPDATE));
    }

    @HasAdminOrSelf
    @PatchMapping("/{id}/avatar")
    @ApiMessage(ActionConstant.USER_UPLOAD_AVATAR)
    public ResponseEntity<UserResponse> uploadUserAvatarInLocalRequest(
            @PathVariable Long id,
            @RequestParam(name = "fileName", required = false) MultipartFile file
    ) throws FileException,
             URISyntaxException,
             IOException {
        User userUpdated = userService.uploadUserAvatarInLocal(id, file);
        return ResponseEntity.ok(userMapper.toResponse(userUpdated, ActionConstant.UPDATE));
    }

    @HasAdminOrSelf
    @PatchMapping("/{id}/avatar/cloud")
    @ApiMessage(ActionConstant.USER_UPLOAD_AVATAR_CLOUDINARY)
    public ResponseEntity<UserResponse> uploadUserAvatarToCloudinaryRequest(
            @PathVariable Long id,
            @RequestParam(name = "fileName", required = false) MultipartFile file
    ) throws FileException, IOException {
        User userUpdated = userService.uploadUserAvatarToCloud(id, file);
        return ResponseEntity.ok(userMapper.toResponse(userUpdated, ActionConstant.UPDATE));
    }

    @HasAdminOrSelf
    @DeleteMapping("/{id}/avatar")
    @ApiMessage(ActionConstant.USER_DELETE_AVATAR)
    public ResponseEntity<UserResponse> deleteUserAvatarRequest(
            @PathVariable Long id,
            @RequestParam(name = "fileName", required = false) String fileName
    ) throws URISyntaxException, IOException {
        User userRemoveAvatar = userService.removeUserAvatarInLocal(id, fileName);
        return ResponseEntity.ok(userMapper.toResponse(userRemoveAvatar, ActionConstant.UPDATE));
    }

    @HasAdminOrSelf
    @DeleteMapping("/{id}/avatar/cloud")
    @ApiMessage(ActionConstant.USER_DELETE_AVATAR_CLOUDINARY)
    public ResponseEntity<UserResponse> deleteUserAvatarFromCloudinaryRequest(
            @PathVariable Long id,
            @RequestParam(name = "fileName", required = false) String fileName
    ) throws IOException {
        User userRemoveAvatar = userService.removeUserAvatarInCloud(id, fileName);
        return ResponseEntity.ok(userMapper.toResponse(userRemoveAvatar, ActionConstant.UPDATE));
    }

    @HasAdminOrSelf
    @DeleteMapping("/{id}")
    @ApiMessage(ActionConstant.USER_DELETE)
    public ResponseEntity<Void> deleteUserRequest(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('" + AuthorityConstant.ADMIN + "')")
    @PostMapping("/import")
    @ApiMessage(ActionConstant.USER_IMPORT)
    public ResponseEntity<ImportFileResponse<UserResponse>> importUsersFromFileRequest(
            @Valid @RequestBody List<UserCreateRequest> userCreateRequestList
    ) {
        ImportFileResponse<UserResponse> importFileResponse = userService.importUsersFromFile(userCreateRequestList);
        HttpStatus status = HttpStatusUtil.determineHttpStatus(importFileResponse);
        return ResponseEntity
                .status(status)
                .body(importFileResponse);
    }
}
