package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.request.user.UserCreateRequest;
import com.fullstack.bookShop.dto.request.user.UserPasswordRequest;
import com.fullstack.bookShop.dto.response.UserResponse;
import com.fullstack.bookShop.dto.response.imports.ImportFileResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.exception.custom.FileException;
import com.fullstack.bookShop.mapper.UserMapper;
import com.fullstack.bookShop.model.Role;
import com.fullstack.bookShop.model.User;
import com.fullstack.bookShop.repository.RatingRepository;
import com.fullstack.bookShop.repository.UserRepository;
import com.fullstack.bookShop.service.interfaces.CloudinaryService;
import com.fullstack.bookShop.service.interfaces.FileService;
import com.fullstack.bookShop.service.interfaces.RoleService;
import com.fullstack.bookShop.service.interfaces.UserService;
import com.fullstack.bookShop.util.cloudinary.CloudinaryUtil;
import com.fullstack.bookShop.util.constant.FolderConstant;
import com.fullstack.bookShop.util.constant.auth.AuthorityConstant;
import com.fullstack.bookShop.util.constant.CommonMessage;
import com.fullstack.bookShop.util.constant.PasswordConstant;
import com.fullstack.bookShop.util.constant.SymbolConstant;
import com.fullstack.bookShop.util.enums.RoleEnum;
import com.fullstack.bookShop.util.enums.UserEnum;
import com.fullstack.bookShop.util.file.FileValidUtil;
import com.fullstack.bookShop.util.pagination.PaginationUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Value("${bookshop.upload-file.base-uri}")
    private String baseURI;

    private final UserMapper userMapper;

    private final RoleService roleService;

    private final FileService fileService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RatingRepository ratingRepository;

    private final CloudinaryService cloudinaryService;

    @Override
    public PaginationResponse fetchAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> userPage = userRepository.findAll(spec, pageable);

        List<UserResponse> userResponseList = userPage.getContent()
                .stream()
                .map(userMapper::convertUserToViewResponse)
                .toList();

        PaginationResponse paginationResponse = PaginationUtil.fetchAllDataWithPagination(userPage, pageable);
        paginationResponse.setResult(userResponseList);
        return paginationResponse;
    }

    @Transactional
    @Override
    public User createUser(User userRequest) {
        LOGGER.info(
                "Creating user fullName={}, email={}, phone={}, role={}",
                userRequest.getFullName(),
                userRequest.getEmail(),
                userRequest.getPhone(),
                userRequest.getRole()
        );

        validateUserForCreate(userRequest);
        validateAndSetRole(userRequest, userRequest);

        String hashPassword = passwordEncoder.encode(userRequest.getPassword());
        userRequest.setPassword(hashPassword);

        User userCreated = userRepository.save(userRequest);
        LOGGER.info("Create user successfully with id: {}", userCreated.getId());

        return userCreated;
    }

    @Transactional
    @Override
    public User createGoogleUser(String email, String name, String picture) {
        LOGGER.info(
                "Creating google user with email={}, name={}, picture={}",
                email,
                name,
                picture
        );

        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            user.setFullName(
                    name != null
                            ? name
                            : email.split(SymbolConstant.AT_SIGN)[0]
            );

            if (picture != null) {
                user.setAvatar(picture);
            }

            User userGoogleCreated = userRepository.save(user);
            LOGGER.info("Create google user successfully with id: {}", userGoogleCreated.getId());

            return userGoogleCreated;
        });
    }

    @Override
    public User getUserById(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        String.format(UserEnum.ID_NOT_FOUND.getMessage(), id)
                ));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format(UserEnum.EMAIL_NOT_FOUND.getMessage(), email)
                ));
    }

    @Override
    public User getUserByEmailAndRefreshToken(String email, String refreshToken) {
        return userRepository
                .findByEmailAndRefreshToken(email, refreshToken)
                .orElseThrow(() -> new NoSuchElementException(
                        String.format(UserEnum.EMAIL_TOKEN_NOT_FOUND.getMessage(), email)
                ));
    }

    @Transactional
    @Override
    public User updateUserById(Long id, User userRequest) {
        LOGGER.info("Updating user id: {}", id);

        User userGetById = getUserById(id);
        validateUserForUpdate(userGetById, userRequest);
        validateAndSetRole(userGetById, userRequest);

        userGetById.setFullName(userRequest.getFullName());
        userGetById.setPhone(userRequest.getPhone());

        LOGGER.info("Update user successfully with id: {}", id);
        return userRepository.save(userGetById);
    }

    @Transactional
    @Override
    public User partialUpdateUserById(Long id, User userRequest) {
        LOGGER.info("Updating user partial id: {}", id);

        User userGetById = getUserById(id);
        validateAndSetRole(userGetById, userRequest);

        if (userRequest.getFullName() != null && !userRequest.getFullName().trim().isEmpty()) {
            userGetById.setFullName(userRequest.getFullName());
        }
        if (userRequest.getPhone() != null && !userRequest.getPhone().trim().isEmpty()) {
            validateUserForUpdate(userGetById, userRequest);
            userGetById.setPhone(userRequest.getPhone());
        }

        LOGGER.info("Update user partial successfully with id: {}", id);
        return userRepository.save(userGetById);
    }

    @Transactional
    @Override
    public User updateUserPasswordById(Long id, UserPasswordRequest userPasswordRequest) {
        LOGGER.info("Updating user password id: {}", id);

        User userGetById = getUserById(id);

        if (!passwordEncoder.matches(userPasswordRequest.getCurrentPassword(), userGetById.getPassword())) {
            throw new IllegalArgumentException(String.format(PasswordConstant.PASSWORD_NOT_MATCH));
        }
        if (userPasswordRequest.getNewPassword() != null && !userPasswordRequest.getNewPassword().trim().isEmpty()) {
            userGetById.setPassword(passwordEncoder.encode(userPasswordRequest.getNewPassword()));
        }

        LOGGER.info("Update user password successfully with id: {}", id);
        return userRepository.save(userGetById);
    }

    @Transactional
    @Override
    public User uploadUserAvatarInLocal(Long id, MultipartFile file)
            throws FileException,
                   URISyntaxException,
                   IOException {

        LOGGER.info("Uploading avatar in local for user id={}, avatar={}", id, file);

        User userGetById = getUserById(id);
        FileValidUtil.validateSingleFile(file);
        fileService.createUploadFolder(baseURI + FolderConstant.AVATAR);

        String fileUploaded = fileService.storeSingleFile(file, FolderConstant.AVATAR);
        userGetById.setAvatar(fileUploaded);

        LOGGER.info("Upload avatar in local successfully for user id={}", id);
        return userRepository.save(userGetById);
    }

    @Transactional
    @Override
    public User uploadUserAvatarToCloud(Long id, MultipartFile file) throws FileException, IOException {
        LOGGER.info("Uploading avatar to cloud for user id={}, avatar={}", id, file);

        User userGetById = getUserById(id);
        FileValidUtil.validateSingleFile(file);
        Map<String, Object> filePushToCloudinary = cloudinaryService.uploadImageToCloudinary(file);

        String fileUploaded = CloudinaryUtil.getFilePushedFromCloudinary(filePushToCloudinary);
        userGetById.setAvatar(fileUploaded);

        LOGGER.info("Upload avatar to cloud successfully for user id={}", id);
        return userRepository.save(userGetById);
    }

    @Transactional
    @Override
    public User removeUserAvatarInLocal(Long id, String avatarRequest) throws URISyntaxException, IOException {
        LOGGER.info("Removing avatar in local for user id={}, avatar={}", id, avatarRequest);

        fileService.deleteSingleFile(avatarRequest, FolderConstant.AVATAR);

        User userGetById = getUserById(id);
        String userAvatar = userGetById.getAvatar();

        if (userAvatar != null && userAvatar.equals(avatarRequest)) {
            userGetById.setAvatar(null);
        }

        LOGGER.info("Remove avatar in local successfully for user id={}", id);
        return userRepository.save(userGetById);
    }

    @Transactional
    @Override
    public User removeUserAvatarInCloud(Long id, String avatarRequest) throws IOException {
        LOGGER.info("Removing avatar in cloud for user id={}, avatar={}", id, avatarRequest);

        String publicId = CloudinaryUtil.generatePublicId(avatarRequest);
        cloudinaryService.deleteImageFromCloudinary(publicId);

        User userGetById = getUserById(id);
        String userAvatar = userGetById.getAvatar();

        if (userAvatar != null && userAvatar.equals(avatarRequest)) {
            userGetById.setAvatar(null);
        }

        LOGGER.info("Remove avatar in cloud successfully for user id={}", id);
        return userRepository.save(userGetById);
    }

    @Transactional
    @Override
    public void saveUserRefreshToken(String email, String refreshToken) {
        User userGetByEmail = getUserByEmail(email);
        if (userGetByEmail != null) {
            userGetByEmail.setRefreshToken(refreshToken);
            userRepository.save(userGetByEmail);
        }
    }

    @Transactional
    @Override
    public void deleteUserById(Long id) {
        LOGGER.info("Deleting user id: {}", id);

        if (!checkIdExists(id)) {
            throw new NoSuchElementException(
                    String.format(UserEnum.ID_NOT_FOUND.getMessage(), id)
            );
        }

        User userGetById = getUserById(id);
        Role userRole = userGetById.getRole();

        if (userRole != null && AuthorityConstant.ADMIN.equals(userRole.getName())) {
            throw new IllegalStateException(CommonMessage.NOT_PERMISSION_DELETE_USER);
        }

        ratingRepository.deleteByUserId(id);
        userRepository.deleteById(id);

        LOGGER.info("Delete user successfully with id: {}", id);
    }

    @Override
    public boolean checkIdExists(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean checkPhoneExists(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    public ImportFileResponse<UserResponse> importUsersFromFiles(List<UserCreateRequest> userCreateRequestList) {
        ImportFileResponse<UserResponse> importFileResponse = new ImportFileResponse<>();
        List<UserResponse> successList = new ArrayList<>();
        List<ImportFileResponse.DetailFaultInfo<UserCreateRequest>> detailFaultInfoList = new ArrayList<>();

        int countSuccess = 0;
        int countError = 0;

        for (int i = 0; i < userCreateRequestList.size(); i++) {
            UserCreateRequest userCreateRequest = userCreateRequestList.get(i);
            int index = i + 1;

            try {
                validateUserForImport(userCreateRequest);

                User userBuildFromUserCreateRequest = buildUserFromUserCreateRequest(userCreateRequest);
                UserResponse userResponse = userMapper.convertUserToViewResponse(userBuildFromUserCreateRequest);

                successList.add(userResponse);
                countSuccess++;

                LOGGER.info("Import user successfully with index {}: {}", index, userCreateRequest.getFullName());

            } catch (ConstraintViolationException ex) {
                List<String> errorMessage = ex.getConstraintViolations()
                        .stream()
                        .map(ConstraintViolation::getMessage)
                        .toList();

                detailFaultInfoList.add(new ImportFileResponse.DetailFaultInfo<>(
                        index,
                        errorMessage,
                        userCreateRequest
                ));

                countError++;
                LOGGER.warn("Import failed at row {} due to validation: {}", index, errorMessage);

            } catch (Exception ex) {
                detailFaultInfoList.add(new ImportFileResponse.DetailFaultInfo<>(
                        index,
                        ex.getMessage(),
                        userCreateRequest
                ));

                countError++;
                LOGGER.error("Unexpected error importing user at row {}: {}", index, ex.getMessage(), ex);
            }
        }

        importFileResponse.setCountSuccess(countSuccess);
        importFileResponse.setCountError(countError);
        importFileResponse.setDetail(countError > 0 ? detailFaultInfoList : CommonMessage.IMPORT_SUCCESS);
        importFileResponse.setItems(successList);

        return importFileResponse;
    }

    private void validateUserForCreate(User userRequest) {
        if (checkEmailExists(userRequest.getEmail())) {
            LOGGER.warn("Create failed: User email '{}' already exists", userRequest.getEmail());
            throw new IllegalArgumentException(
                    String.format(UserEnum.EMAIL_EXISTS.getMessage(), userRequest.getEmail())
            );
        }
        if (checkPhoneExists(userRequest.getPhone())) {
            LOGGER.warn("Create failed: User phone '{}' already exists", userRequest.getPhone());
            throw new IllegalArgumentException(
                    String.format(UserEnum.PHONE_EXISTS.getMessage(), userRequest.getPhone())
            );
        }
    }

    private void validateUserForUpdate(User currentUser, User userRequest) {
        boolean isPhoneExists = checkPhoneExists(userRequest.getPhone());

        if (isPhoneExists && !userRequest.getPhone().equals(currentUser.getPhone())) {
            LOGGER.warn("Update failed: User phone '{}' already exists", userRequest.getPhone());
            throw new IllegalArgumentException(
                    String.format(UserEnum.PHONE_EXISTS.getMessage(), userRequest.getPhone())
            );
        }
    }

    private void validateUserForImport(UserCreateRequest userCreateRequest) {
        if (checkEmailExists(userCreateRequest.getEmail())) {
            LOGGER.warn("Import failed: User email '{}' already exists", userCreateRequest.getEmail());
            throw new IllegalArgumentException(
                    String.format(UserEnum.EMAIL_EXISTS.getMessage(), userCreateRequest.getEmail())
            );
        }
        if (checkPhoneExists(userCreateRequest.getPhone())) {
            LOGGER.warn("Import failed: User phone '{}' already exists", userCreateRequest.getPhone());
            throw new IllegalArgumentException(
                    String.format(UserEnum.PHONE_EXISTS.getMessage(), userCreateRequest.getPhone())
            );
        }
    }

    private User buildUserFromUserCreateRequest(UserCreateRequest userCreateRequest) {
        User user = new User();
        user.setFullName(userCreateRequest.getFullName());
        user.setEmail(userCreateRequest.getEmail());
        user.setPhone(userCreateRequest.getPhone());
        user.setPassword(PasswordConstant.DEFAULT_PASSWORD);
        user.setRole(roleService.getRoleByName(AuthorityConstant.USER));
        return userRepository.save(user);
    }

    private void validateAndSetRole(User currentUser, User userRequest) {
        if (userRequest.getRole() != null) {
            Long roleId = userRequest.getRole().getId();
            if (!roleService.checkIdExists(roleId)) {
                throw new IllegalArgumentException(
                        String.format(RoleEnum.ID_NOT_FOUND.getMessage(), roleId)
                );
            }
            Role roleGetById = roleService.getRoleById(roleId);
            currentUser.setRole(roleGetById);
        } else {
            Role roleDefault = roleService.getRoleByName(AuthorityConstant.USER);
            currentUser.setRole(roleDefault);
        }
    }
}
