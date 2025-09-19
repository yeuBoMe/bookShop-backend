package com.fullstack.bookShop.service;

import com.fullstack.bookShop.domain.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.domain.dto.user.RestListUsersImportDTO;
import com.fullstack.bookShop.domain.dto.user.RestUserCreateDTO;
import com.fullstack.bookShop.domain.dto.user.RestUserUpdateDTO;
import com.fullstack.bookShop.domain.dto.user.RestUserViewDTO;
import com.fullstack.bookShop.domain.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface UserService {
    User createUser(User user);
    User updateUserById(Long id, User user);
    User updateUserPartialById(Long id, User user);
    User getUserById(Long id);
    User getUserByEmail(String email);
    User getUserByEmailAndRefreshToken(String email, String refreshToken);

    void deleteUserById(Long id);
    void handleSaveUserRefreshToken(String email, String refreshToken);

    boolean checkEmailExists(String email);
    boolean checkPhoneExists(String phone);
    boolean checkIdExists(Long id);

    RestUserCreateDTO convertUserToRestUserCreateDTO(User user);
    RestUserUpdateDTO convertUserToRestUserUpdateDTO(User user);
    RestUserViewDTO convertUserToRestUserViewDTO(User user);

    ResultPaginationDTO fetchAllUsers(Specification<User> spec, Pageable pageable);

    RestListUsersImportDTO<Object> importListUsersFromFileExcel(List<RestUserCreateDTO> restUserCreateDTOList);
}
