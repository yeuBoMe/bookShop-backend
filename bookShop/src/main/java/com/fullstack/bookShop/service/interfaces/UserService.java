package com.fullstack.bookShop.service.interfaces;

import com.fullstack.bookShop.dto.request.user.UserCreateRequest;
import com.fullstack.bookShop.dto.request.user.UserPasswordRequest;
import com.fullstack.bookShop.dto.response.UserResponse;
import com.fullstack.bookShop.dto.response.imports.ImportFileResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.exception.custom.FileException;
import com.fullstack.bookShop.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface UserService {

    PaginationResponse fetchAllUsers(Specification<User> spec, Pageable pageable);

    User createUser(User userRequest);

    User createGoogleUser(String email, String name, String picture);

    User getUserById(Long id);

    User getUserByEmail(String email);

    User getUserByEmailAndRefreshToken(String email, String refreshToken);

    User updateUserById(Long id, User userRequest);

    User partialUpdateUserById(Long id, User userRequest);

    User updateUserPasswordById(Long id, UserPasswordRequest userPasswordRequest);

    User uploadUserAvatarInLocal(Long id, MultipartFile file) throws FileException, URISyntaxException, IOException;

    User uploadUserAvatarToCloud(Long id, MultipartFile file) throws FileException, IOException;

    User removeUserAvatarInLocal(Long id, String avatarRequest) throws URISyntaxException, IOException;

    User removeUserAvatarInCloud(Long id, String avatarRequest) throws IOException;

    void saveUserRefreshToken(String email, String refreshToken);

    void deleteUserById(Long id);

    boolean checkIdExists(Long id);

    boolean checkEmailExists(String email);

    boolean checkPhoneExists(String phone);

    ImportFileResponse<UserResponse> importUsersFromFile(List<UserCreateRequest> userCreateRequestList);
}
