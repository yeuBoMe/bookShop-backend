package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.dto.imports.RestImportDTO;
import com.fullstack.bookShop.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.dto.user.RestUserCreateDTO;
import com.fullstack.bookShop.dto.user.RestUserPassDTO;
import com.fullstack.bookShop.dto.user.RestUserUpdateDTO;
import com.fullstack.bookShop.dto.user.RestUserViewDTO;
import com.fullstack.bookShop.model.User;
import com.fullstack.bookShop.service.CloudinaryService;
import com.fullstack.bookShop.service.FileService;
import com.fullstack.bookShop.service.UserService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.exception.custom.StorageException;
import com.fullstack.bookShop.util.imports.FileValidUtil;
import com.fullstack.bookShop.util.imports.HttpStatusUtil;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    @Value("${bookshop.upload-file.base-uri}")
    private String baseUri;

    private final UserService userService;

    private final FileService fileService;

    private final CloudinaryService cloudinaryService;

    @PostMapping
    @ApiMessage("Create user")
    public ResponseEntity<RestUserCreateDTO> createUserRequest(@Valid @RequestBody User userRequest) {
        User userCreated = userService.createUser(userRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.convertUserToRestUserCreateDTO(userCreated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @ApiMessage("Fetch list users")
    public ResponseEntity<ResultPaginationDTO> fetchAllUsersRequest(
            @Filter Specification<User> spec,
            Pageable pageable
    ) {
        return ResponseEntity.ok(userService.fetchAllUsers(spec, pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    @ApiMessage("Get user")
    public ResponseEntity<RestUserViewDTO> getUserByIdRequest(@PathVariable Long id) {
        User userGetById = userService.getUserById(id);
        return ResponseEntity.ok(userService.convertUserToRestUserViewDTO(userGetById));
    }

    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.claims['user']['id']")
    @PutMapping("/{id}")
    @ApiMessage("Update user")
    public ResponseEntity<RestUserUpdateDTO> updateUserByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody User userRequest
    ) {
        User userUpdated = userService.updateUserById(id, userRequest);
        return ResponseEntity.ok(userService.convertUserToRestUserUpdateDTO(userUpdated));
    }

    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.claims['user']['id']")
    @PatchMapping("/{id}")
    @ApiMessage("Update user partial")
    public ResponseEntity<RestUserUpdateDTO> updateUserPartialByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody User userRequest
    ) {
        User userUpdated = userService.updateUserPartialById(id, userRequest);
        return ResponseEntity.ok(userService.convertUserToRestUserUpdateDTO(userUpdated));
    }

    @PreAuthorize("#id == authentication.principal.claims['user']['id']")
    @PatchMapping("/{id}/password")
    @ApiMessage("Update user password")
    public ResponseEntity<RestUserUpdateDTO> updateUserPasswordByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody RestUserPassDTO restUserPassDTO
    ) {
        User userUpdated = userService.updateUserPasswordById(id, restUserPassDTO);
        return ResponseEntity.ok(userService.convertUserToRestUserUpdateDTO(userUpdated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ApiMessage("Delete user")
    public ResponseEntity<Void> deleteUserByIdRequest(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("#id == authentication.principal.claims['user']['id']")
    @PatchMapping("/{id}/avatar")
    @ApiMessage("Update user avatar")
    public ResponseEntity<RestUserUpdateDTO> updateUserAvatarRequest(
            @PathVariable Long id,
            @RequestParam(name = "fileName", required = false) MultipartFile file
    ) throws StorageException,
             URISyntaxException,
             IOException {

        FileValidUtil.validateFile(file);
        fileService.createUploadFolder(baseUri + "avatar");
        String fileUploaded = fileService.storeSingleFile(file, "avatar");

        User user = new User();
        user.setAvatar(fileUploaded);
        User userUpdated = userService.updateUserPartialById(id, user);

        return ResponseEntity.ok(userService.convertUserToRestUserUpdateDTO(userUpdated));
    }

    @PreAuthorize("#id == authentication.principal.claims['user']['id']")
    @PatchMapping("/{id}/cloud-avatar")
    @ApiMessage("Update user avatar to cloud")
    public ResponseEntity<RestUserUpdateDTO> updateUserAvatarToCloudinaryRequest(
            @PathVariable Long id,
            @RequestParam(name = "fileName", required = false) MultipartFile file
    ) throws StorageException, IOException {

        FileValidUtil.validateFile(file);
        Map<String, Object> imageUploadedInCloud = cloudinaryService.uploadImageToCloudinary(file);
        String fileUploaded = imageUploadedInCloud.get("public_id") + "." + imageUploadedInCloud.get("format");

        User user = new User();
        user.setAvatar(fileUploaded);
        User userUpdated = userService.updateUserPartialById(id, user);

        return ResponseEntity.ok(userService.convertUserToRestUserUpdateDTO(userUpdated));
    }

    @PreAuthorize("#id == authentication.principal.claims['user']['id']")
    @DeleteMapping("/{id}/avatar")
    @ApiMessage("Delete user avatar")
    public ResponseEntity<RestUserUpdateDTO> deleteUserAvatarRequest(
            @PathVariable Long id,
            @RequestParam(name = "fileName", required = false) String fileName
    ) throws URISyntaxException, IOException {

        fileService.deleteSingleFile(fileName, "avatar");

        User userGetById = userService.getUserById(id);
        if (userGetById.getAvatar() != null && userGetById.getAvatar().equals(fileName)) {
            userGetById.setAvatar(null);
        }

        User userUpdated = userService.updateUserPartialById(id, userGetById);
        return ResponseEntity.ok(userService.convertUserToRestUserUpdateDTO(userUpdated));
    }

    @PreAuthorize("#id == authentication.principal.claims['user']['id']")
    @DeleteMapping("/{id}/cloud-avatar")
    @ApiMessage("Delete user avatar in cloud")
    public ResponseEntity<RestUserUpdateDTO> deleteUserAvatarInCloudRequest(
            @PathVariable Long id,
            @RequestParam(name = "fileName", required = false) String fileName
    ) throws IOException {

        String publicId = fileName.contains(".")
                ? fileName.substring(0, fileName.lastIndexOf("."))
                : fileName;

        cloudinaryService.deleteImageFromCloudinary(publicId);

        User userGetById = userService.getUserById(id);
        if (userGetById.getAvatar() != null && userGetById.getAvatar().equals(fileName)) {
            userGetById.setAvatar(null);
        }

        User userUpdated = userService.updateUserPartialById(id, userGetById);
        return ResponseEntity.ok(userService.convertUserToRestUserUpdateDTO(userUpdated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import")
    @ApiMessage("Import users from file")
    public ResponseEntity<RestImportDTO<RestUserCreateDTO>> importUsersFromFileRequest(
            @Valid @RequestBody List<RestUserCreateDTO> restUserCreateDTOList
    ) {
        RestImportDTO<RestUserCreateDTO> restImportDTO = userService.importListUsersFromFile(restUserCreateDTOList);
        HttpStatus httpStatus = HttpStatusUtil.determineHttpStatus(restImportDTO);
        return ResponseEntity
                .status(httpStatus)
                .body(restImportDTO);
    }
}
