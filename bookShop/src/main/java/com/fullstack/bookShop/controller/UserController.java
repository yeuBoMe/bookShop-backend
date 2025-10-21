package com.fullstack.bookShop.controller;

import com.fullstack.bookShop.domain.dto.imports.RestImportDTO;
import com.fullstack.bookShop.domain.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.domain.dto.user.RestUserCreateDTO;
import com.fullstack.bookShop.domain.dto.user.RestUserPassDTO;
import com.fullstack.bookShop.domain.dto.user.RestUserUpdateDTO;
import com.fullstack.bookShop.domain.dto.user.RestUserViewDTO;
import com.fullstack.bookShop.domain.entity.User;
import com.fullstack.bookShop.service.FileService;
import com.fullstack.bookShop.service.UserService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.exception.custom.StorageException;
import com.fullstack.bookShop.util.imports.FileValidUtil;
import com.fullstack.bookShop.util.imports.HttpStatusUtil;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
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
public class UserController {

    @Value("${bookshop.upload-file.base-uri}")
    private String baseUri;

    private final UserService userService;

    private final FileService fileService;

    public UserController(UserService userService, FileService fileService) {
        this.userService = userService;
        this.fileService = fileService;
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

    @PatchMapping("/{id}/password")
    @ApiMessage("Update user password")
    public ResponseEntity<RestUserUpdateDTO> updateUserPasswordByIdRequest(
            @PathVariable Long id,
            @Valid @RequestBody RestUserPassDTO restUserPassDTO
    ) {
        User userUpdated = this.userService.updateUserPasswordById(id, restUserPassDTO);
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
            @RequestParam(name = "fileName", required = false) MultipartFile file
    ) throws
            StorageException,
            URISyntaxException,
            IOException {

        FileValidUtil.validateFile(file);
        this.fileService.createUploadFolder(this.baseUri + "avatar");
        String fileUploaded = this.fileService.storeSingleFile(file, "avatar");

        User user = new User();
        user.setAvatar(fileUploaded);
        User userUpdated = this.userService.updateUserPartialById(id, user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.userService.convertUserToRestUserUpdateDTO(userUpdated));
    }

    @ApiMessage("Delete user avatar")
    @DeleteMapping("/{id}/avatar")
    public ResponseEntity<RestUserUpdateDTO> deleteUserAvatarRequest(
            @PathVariable Long id,
            @RequestParam(name = "fileName", required = false) String fileName
    ) throws URISyntaxException, IOException {

        this.fileService.deleteSingleFile(fileName, "avatar");

        User userGetById = this.userService.getUserById(id);
        if (userGetById.getAvatar() != null && userGetById.getAvatar().equals(fileName)) {
            userGetById.setAvatar(null);
        }

        User userUpdated = this.userService.updateUserPartialById(id, userGetById);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.userService.convertUserToRestUserUpdateDTO(userUpdated));
    }

    @ApiMessage("Import users from file")
    @PostMapping("/import")
    public ResponseEntity<RestImportDTO<RestUserCreateDTO>> importUsersFromFileRequest(
            @Valid @RequestBody List<RestUserCreateDTO> restUserCreateDTOList
    ) {
        RestImportDTO<RestUserCreateDTO> restImportDTO =
                this.userService.importListUsersFromFile(restUserCreateDTOList);

        HttpStatus httpStatus = HttpStatusUtil.determineHttpStatus(restImportDTO);

        return ResponseEntity
                .status(httpStatus)
                .body(restImportDTO);
    }
}
