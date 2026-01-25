package com.fullstack.bookShop.service.interfaces;

import com.fullstack.bookShop.dto.request.user.UserCreateRequest;
import com.fullstack.bookShop.dto.request.user.UserPasswordRequest;
import com.fullstack.bookShop.dto.response.UserResponse;
import com.fullstack.bookShop.dto.response.imports.ImportFileResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

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

    User uploadUserAvatar(Long id, String avatarRequest);

    User removeUserAvatar(Long id, String avatarRequest);

    void saveUserRefreshToken(String email, String refreshToken);

    void deleteUserById(Long id);

    boolean checkIdExists(Long id);

    boolean checkEmailExists(String email);

    boolean checkPhoneExists(String phone);

    ImportFileResponse<UserResponse> importUsersFromFiles(List<UserCreateRequest> userCreateRequestList);
}
