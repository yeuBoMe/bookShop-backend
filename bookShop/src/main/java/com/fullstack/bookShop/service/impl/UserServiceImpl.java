package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.domain.dto.imports.RestImportDTO;
import com.fullstack.bookShop.domain.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.domain.dto.user.RestUserCreateDTO;
import com.fullstack.bookShop.domain.dto.user.RestUserPassDTO;
import com.fullstack.bookShop.domain.dto.user.RestUserUpdateDTO;
import com.fullstack.bookShop.domain.dto.user.RestUserViewDTO;
import com.fullstack.bookShop.domain.entity.Role;
import com.fullstack.bookShop.domain.entity.User;
import com.fullstack.bookShop.repository.RatingRepository;
import com.fullstack.bookShop.repository.UserRepository;
import com.fullstack.bookShop.service.RoleService;
import com.fullstack.bookShop.service.UserService;
import com.fullstack.bookShop.util.pagination.PageUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final RatingRepository ratingRepository;

    private final PasswordEncoder passwordEncoder;

    private final RoleService roleService;

    private void validateAndSetRole(User userRequest, User currentUser) {
        if (userRequest.getRole() != null) {
            if (!roleService.checkIdExists(userRequest.getRole().getId())) {
                throw new NoSuchElementException("Role with id " + userRequest.getRole().getId() + " not found!");
            }
            Role roleGetById = roleService.getRoleById(userRequest.getRole().getId());
            currentUser.setRole(roleGetById);
        }
    }

    @Transactional
    @Override
    public User createUser(User user) {
        if (checkEmailExists(user.getEmail())) {
            throw new IllegalArgumentException("User with email '" + user.getEmail() + "' already exists!");
        }
        if (checkPhoneExists(user.getPhone())) {
            throw new IllegalArgumentException("User with phone '" + user.getPhone() + "' already exists!");
        }

        validateAndSetRole(user, user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    @Transactional
    @Override
    public User updateUserById(Long id, User userUpdated) {
        User userGetById = getUserById(id);
        validateAndSetRole(userUpdated, userGetById);

        if (checkPhoneExists(userUpdated.getPhone())
                && !userGetById.getPhone().equals(userUpdated.getPhone())
        ) {
            throw new IllegalArgumentException("User with phone '" + userUpdated.getPhone() + "' already exists!");
        }

        userGetById.setFullName(userUpdated.getFullName());
        userGetById.setPhone(userUpdated.getPhone());

        return userRepository.save(userGetById);
    }

    @Transactional
    @Override
    public User updateUserPartialById(Long id, User userUpdated) {
        User userGetById = getUserById(id);
        validateAndSetRole(userUpdated, userGetById);

        if (userUpdated.getAvatar() != null) {
            System.out.println("Updating avatar to: " + userUpdated.getAvatar());
            userGetById.setAvatar(userUpdated.getAvatar());
        }
        if (userUpdated.getFullName() != null && !userUpdated.getFullName().isEmpty()) {
            userGetById.setFullName(userUpdated.getFullName());
        }
        if (userUpdated.getPhone() != null && !userUpdated.getPhone().isEmpty()) {
            if (checkPhoneExists(userUpdated.getPhone())
                    && !userGetById.getPhone().equals(userUpdated.getPhone())
            ) {
                throw new IllegalArgumentException("User with phone '" + userUpdated.getPhone() + "' already exists!");
            }
            userGetById.setPhone(userUpdated.getPhone());
        }

        return userRepository.save(userGetById);
    }

    @Transactional
    @Override
    public User updateUserPasswordById(Long id, RestUserPassDTO restUserPassDTO) {
        User userGetById = getUserById(id);

        if (!passwordEncoder.matches(restUserPassDTO.getCurrentPassword(), userGetById.getPassword())) {
            throw new IllegalArgumentException("Current password doesn't match!");
        }
        if (restUserPassDTO.getNewPassword() != null) {
            userGetById.setPassword(passwordEncoder.encode(restUserPassDTO.getNewPassword()));
        }

        return userRepository.save(userGetById);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException("User with id " + id + " not found!"));
    }

    @Transactional
    @Override
    public void deleteUserById(Long id) {
        if (!checkIdExists(id)) {
            throw new NoSuchElementException("User with id " + id + " not found!");
        }

        User userGetById = getUserById(id);
        if (userGetById.getRole() != null
                && userGetById.getRole().getName().equals("ADMIN")
        ) {
            throw new IllegalStateException("You do not have permission to delete this user!");
        }

        ratingRepository.deleteByUserId(id);
        userRepository.deleteById(id);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User with email '" + email + "' not found!"));
    }

    @Override
    public User getUserByEmailAndRefreshToken(String email, String refreshToken) {
        return userRepository.findByEmailAndRefreshToken(email, refreshToken)
                .orElseThrow(() -> new NoSuchElementException("User not found!"));
    }

    @Override
    public void handleSaveUserRefreshToken(String email, String refreshToken) {
        User userGetByEmail = getUserByEmail(email);
        if (userGetByEmail != null) {
            userGetByEmail.setRefreshToken(refreshToken);
            userRepository.save(userGetByEmail);
        }
    }

    @Override
    public ResultPaginationDTO fetchAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> pageHavUsers = userRepository.findAll(spec, pageable);

        List<RestUserViewDTO> restUserViewDTOList = pageHavUsers.getContent()
                .stream()
                .map(this::convertUserToRestUserViewDTO)
                .toList();

        ResultPaginationDTO resultPaginationDTO = PageUtil.handleFetchAllDataWithPagination(pageHavUsers, pageable);
        resultPaginationDTO.setResult(restUserViewDTOList);

        return resultPaginationDTO;
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
    public boolean checkIdExists(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public RestUserCreateDTO convertUserToRestUserCreateDTO(User user) {
        RestUserCreateDTO.RoleUserCreate roleUserCreate = Optional.ofNullable(user.getRole())
                .map(role -> new RestUserCreateDTO.RoleUserCreate(role.getId(), role.getName()))
                .orElse(null);

        return new RestUserCreateDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getAvatar(),
                user.getPhone(),
                user.getCreatedAt(),
                user.getCreatedBy(),
                roleUserCreate
        );
    }

    @Override
    public RestUserUpdateDTO convertUserToRestUserUpdateDTO(User user) {
        RestUserUpdateDTO.RoleUserUpdate roleUserUpdate = Optional.ofNullable(user.getRole())
                .map(role -> new RestUserUpdateDTO.RoleUserUpdate(role.getId(), role.getName()))
                .orElse(null);

        return new RestUserUpdateDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getAvatar(),
                user.getPhone(),
                user.getCreatedAt(),
                user.getCreatedBy(),
                roleUserUpdate
        );
    }

    @Override
    public RestUserViewDTO convertUserToRestUserViewDTO(User user) {
        RestUserViewDTO.RoleUserView roleUserView = Optional.ofNullable(user.getRole())
                .map(role -> new RestUserViewDTO.RoleUserView(role.getId(), role.getName()))
                .orElse(null);

        return new RestUserViewDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getAvatar(),
                user.getPhone(),
                user.getCreatedAt(),
                user.getCreatedBy(),
                user.getUpdatedAt(),
                user.getUpdatedBy(),
                roleUserView
        );
    }

    @Override
    public RestImportDTO<RestUserCreateDTO> importListUsersFromFile(List<RestUserCreateDTO> restUserCreateDTOList) {
        RestImportDTO<RestUserCreateDTO> restImportDTO = new RestImportDTO<>();
        List<RestUserCreateDTO> successUsers = new ArrayList<>();
        List<RestImportDTO.DetailFaultInfo<RestUserCreateDTO>> detailFaultInfoList = new ArrayList<>();

        int countSuccess = 0;
        int countError = 0;

        for (int i = 0; i < restUserCreateDTOList.size(); i++) {
            RestUserCreateDTO restUserCreateDTO = restUserCreateDTOList.get(i);

            try {
                if (checkEmailExists(restUserCreateDTO.getEmail())) {
                    throw new IllegalArgumentException(
                            "User with email '" + restUserCreateDTO.getEmail() + "' already exists!"
                    );
                }

                if (checkPhoneExists(restUserCreateDTO.getPhone())) {
                    throw new IllegalArgumentException(
                            "User with phone '" + restUserCreateDTO.getPhone() + "' already exists!"
                    );
                }

                User userBuiltFromRestUserCreatedDTO = buildUserFromRestUserCreatedDTO(restUserCreateDTO);
                RestUserCreateDTO userDTO = convertUserToRestUserCreateDTO(userBuiltFromRestUserCreatedDTO);
                successUsers.add(userDTO);
                countSuccess++;

            } catch (ConstraintViolationException e) {
                countError++;
                List<String> errorMessage = e.getConstraintViolations().stream()
                        .map(ConstraintViolation::getMessage)
                        .toList();
                detailFaultInfoList.add(new RestImportDTO.DetailFaultInfo<>(
                        i + 1,
                        errorMessage,
                        restUserCreateDTO
                ));

            } catch (Exception e) {
                countError++;
                detailFaultInfoList.add(new RestImportDTO.DetailFaultInfo<>(
                        i + 1,
                        e.getMessage(),
                        restUserCreateDTO
                ));
            }
        }

        restImportDTO.setCountSuccess(countSuccess);
        restImportDTO.setCountError(countError);
        restImportDTO.setDetail(countError > 0 ? detailFaultInfoList : "Import users successfully!");
        restImportDTO.setItems(successUsers);

        return restImportDTO;
    }

    private User buildUserFromRestUserCreatedDTO(RestUserCreateDTO restUserCreateDTO) {
        User user = new User();
        user.setFullName(restUserCreateDTO.getFullName().trim());
        user.setEmail(restUserCreateDTO.getEmail().trim());
        user.setPassword(passwordEncoder.encode("123456"));
        user.setPhone(restUserCreateDTO.getPhone().trim());
        return userRepository.save(user);
    }
}
