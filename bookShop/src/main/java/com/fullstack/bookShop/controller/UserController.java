package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.dto.imports.RestImportDTO;
import com.fullstack.bookShop.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.dto.user.RestUserCreateDTO;
import com.fullstack.bookShop.dto.user.RestUserPassDTO;
import com.fullstack.bookShop.dto.user.RestUserUpdateDTO;
import com.fullstack.bookShop.dto.user.RestUserViewDTO;
import com.fullstack.bookShop.model.User;
import com.fullstack.bookShop.service.FileService;
import com.fullstack.bookShop.service.UserService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.exception.custom.StorageException;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    @Value("${bookshop.upload-file.base-uri}")
    private String baseUri;

    private final UserService userService;

    private final FileService fileService;

    @PostMapping
    @ApiMessage("Create user")
    public ResponseEntity<RestUserCreateDTO> createUserRequest(@Valid @RequestBody User userRequest) {
        User userCreated = userService.createUser(userRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.convertUserToRestUserCreateDTO(userCreated));
    }

    @GetMapping
    @ApiMessage("Fetch list users")
    public ResponseEntity<ResultPaginationDTO> fetchAllUsersRequest(
            @Filter Specification<User> spec,
            Pageable pageable
    ) {
        return ResponseEntity.ok(userService.fetchAllUsers(spec, pageable));
    }

    @GetMapping("/{id}")
    @ApiMessage("Get user")
    public ResponseEntity<RestUserViewDTO> getUserByIdRequest(@PathVariable Long id) {
        User userGetById = userService.getUserById(id);
        return ResponseEntity.ok(userService.convertUserToRestUserViewDTO(userGetById));
    }

    @PutMapping("/{id}")
    @ApiMessage("Update user")
    public ResponseEntity<RestUserUpdateDTO> updateUserByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody User userRequest
    ) {
        User userUpdated = userService.updateUserById(id, userRequest);
        return ResponseEntity.ok(userService.convertUserToRestUserUpdateDTO(userUpdated));
    }

    @PatchMapping("/{id}")
    @ApiMessage("Update user partial")
    public ResponseEntity<RestUserUpdateDTO> updateUserPartialByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody User userRequest
    ) {
        User userUpdated = userService.updateUserPartialById(id, userRequest);
        return ResponseEntity.ok(userService.convertUserToRestUserUpdateDTO(userUpdated));
    }

    @PatchMapping("/{id}/password")
    @ApiMessage("Update user password")
    public ResponseEntity<RestUserUpdateDTO> updateUserPasswordByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody RestUserPassDTO restUserPassDTO
    ) {
        User userUpdated = userService.updateUserPasswordById(id, restUserPassDTO);
        return ResponseEntity.ok(userService.convertUserToRestUserUpdateDTO(userUpdated));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete user")
    public ResponseEntity<Void> deleteUserByIdRequest(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @ApiMessage("Update user avatar")
    @PatchMapping("/{id}/avatar")
    public ResponseEntity<RestUserUpdateDTO> uploadUserAvatarRequest(
            @PathVariable Long id,
            @RequestParam(name = "fileName", required = false) MultipartFile file
    ) throws
            StorageException,
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

    @ApiMessage("Delete user avatar")
    @DeleteMapping("/{id}/avatar")
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

    @ApiMessage("Import users from file")
    @PostMapping("/import")
    public ResponseEntity<RestImportDTO<RestUserCreateDTO>> importUsersFromFileRequest(
            @Valid @RequestBody List<RestUserCreateDTO> restUserCreateDTOList
    ) {
        RestImportDTO<RestUserCreateDTO> restImportDTO
                = userService.importListUsersFromFile(restUserCreateDTOList);

        HttpStatus httpStatus = HttpStatusUtil.determineHttpStatus(restImportDTO);

        return ResponseEntity
                .status(httpStatus)
                .body(restImportDTO);
    }
}
