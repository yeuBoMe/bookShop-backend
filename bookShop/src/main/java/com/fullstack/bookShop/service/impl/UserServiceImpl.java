package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.domain.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.domain.dto.user.RestListUsersImportDTO;
import com.fullstack.bookShop.domain.dto.user.RestUserCreateDTO;
import com.fullstack.bookShop.domain.dto.user.RestUserUpdateDTO;
import com.fullstack.bookShop.domain.dto.user.RestUserViewDTO;
import com.fullstack.bookShop.domain.entity.Role;
import com.fullstack.bookShop.domain.entity.User;
import com.fullstack.bookShop.repository.UserRepository;
import com.fullstack.bookShop.service.RoleService;
import com.fullstack.bookShop.service.UserService;
import com.fullstack.bookShop.util.pagination.PageUtil;
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
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RoleService roleService;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RoleService roleService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    private void validateAndSetRole(User userRequest, User currentUser) {
        if (userRequest.getRole() != null) {
            if (!this.roleService.checkIdExists(userRequest.getRole().getId())) {
                throw new NoSuchElementException("Role with id " + userRequest.getRole().getId() + " not found");
            }
            Role roleGetById = this.roleService.getRoleById(userRequest.getRole().getId());
            currentUser.setRole(roleGetById);
        }
    }

    @Transactional
    @Override
    public User createUser(User user) {
        if (this.checkEmailExists(user.getEmail())) {
            throw new IllegalArgumentException("User with email '" + user.getEmail() + "' already exists");
        }

        if (this.checkPhoneExists(user.getPhone())) {
            throw new IllegalArgumentException("User with phone '" + user.getPhone() + "' already exists");
        }

        this.validateAndSetRole(user, user);

        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        return this.userRepository.save(user);
    }

    @Transactional
    @Override
    public User updateUserById(Long id, User userUpdated) {
        User userGetById = this.getUserById(id);
        this.validateAndSetRole(userUpdated, userGetById);
        userGetById.setFullName(userUpdated.getFullName());

        if (this.checkPhoneExists(userUpdated.getPhone())
                && !userGetById.getPhone().equals(userUpdated.getPhone())
        ) {
            userGetById.setPhone(userUpdated.getPhone());
        }

        return this.userRepository.save(userGetById);
    }

    @Transactional
    @Override
    public User updateUserPartialById(Long id, User userUpdated) {
        User userGetById = this.getUserById(id);

        this.validateAndSetRole(userUpdated, userGetById);

        if (userUpdated.getAvatar() != null) {
            System.out.println("Updating avatar to: " + userUpdated.getAvatar());
            userGetById.setAvatar(userUpdated.getAvatar());
        }

        if (userUpdated.getFullName() != null && !userUpdated.getFullName().isEmpty()) {
            userGetById.setFullName(userUpdated.getFullName());
        }

        if (userUpdated.getPhone() != null
                && this.checkPhoneExists(userUpdated.getPhone())
                && !userGetById.getPhone().equals(userUpdated.getPhone())
        ) {
            userGetById.setPhone(userUpdated.getPhone());
        }

        return this.userRepository.save(userGetById);
    }

    @Override
    public User getUserById(Long id) {
        return this.userRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException("User with id " + id + " not found"));
    }

    @Transactional
    @Override
    public void deleteUserById(Long id) {
        if (!this.checkIdExists(id)) {
            throw new NoSuchElementException("User with id " + id + " not found");
        }
        this.userRepository.deleteById(id);
    }

    @Override
    public User getUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    @Override
    public User getUserByEmailAndRefreshToken(String email, String refreshToken) {
        return this.userRepository.findByEmailAndRefreshToken(email, refreshToken);
    }

    @Override
    public void handleSaveUserRefreshToken(String email, String refreshToken) {
        User userGetByEmail = this.getUserByEmail(email);
        if (userGetByEmail != null) {
            userGetByEmail.setRefreshToken(refreshToken);
            this.userRepository.save(userGetByEmail);
        }
    }

    @Override
    public ResultPaginationDTO fetchAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> pageHavUsers = this.userRepository.findAll(spec, pageable);

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
        return this.userRepository.existsByEmail(email);
    }

    @Override
    public boolean checkPhoneExists(String phone) {
        return this.userRepository.existsByPhone(phone);
    }

    @Override
    public boolean checkIdExists(Long id) {
        return this.userRepository.existsById(id);
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

    @Transactional
    @Override
    public RestListUsersImportDTO importListUsersFromFileExcel(List<RestUserCreateDTO> restUserCreateDTOList) {
        RestListUsersImportDTO restListUsersImportDTO = new  RestListUsersImportDTO();
        List<RestUserCreateDTO> successfulUsers = new ArrayList<>();
        List<RestUserCreateDTO> errorUsers = new ArrayList<>();
        int countSuccess = 0;
        int countError = 0;

        for (RestUserCreateDTO restUserCreateDTO : restUserCreateDTOList) {
            try {
                if (this.checkEmailExists(restUserCreateDTO.getEmail())) {
                    throw new IllegalArgumentException("User with email '" + restUserCreateDTO.getEmail() + "' already exists!");
                }

                if(this.checkPhoneExists(restUserCreateDTO.getPhone())) {
                    throw new IllegalArgumentException("User with Phone '" + restUserCreateDTO.getPhone() + "' already exists!");
                }

                User user = new User();
                user.setFullName(restUserCreateDTO.getFullName());
                user.setEmail(restUserCreateDTO.getEmail());
                user.setPassword(this.passwordEncoder.encode("123456"));
                user.setPhone(restUserCreateDTO.getPhone());
                this.userRepository.save(user);

                RestUserCreateDTO userDTO = this.convertUserToRestUserCreateDTO(user);
                successfulUsers.add(userDTO);
                countSuccess++;
            } catch (Exception e) {
                countError++;
                errorUsers.add(restUserCreateDTO);
            }
        }

        restListUsersImportDTO.setCountSuccess(countSuccess);
        restListUsersImportDTO.setCountError(countError);
        restListUsersImportDTO.setUsers(successfulUsers);
        restListUsersImportDTO.setDetail("OK");

        if (countError > 0) {
            List<RestListUsersImportDTO.UserListImportInfo> userListImportInfos = new ArrayList<>();
            for (RestUserCreateDTO restUserCreateDTO : errorUsers) {
                int indexOfUser = restUserCreateDTOList.indexOf(restUserCreateDTO);
                String messageError =
                        this.checkEmailExists(restUserCreateDTO.getEmail())
                            ? "User with email '" + restUserCreateDTO.getEmail() + "' already exists!"
                            : this.checkPhoneExists(restUserCreateDTO.getPhone())
                                ? "User with phone '" + restUserCreateDTO.getPhone() + "' already exists!"
                                : "Unknown error";
                userListImportInfos.add(new RestListUsersImportDTO.UserListImportInfo(indexOfUser, messageError, restUserCreateDTO));
            }
            restListUsersImportDTO.setDetail(userListImportInfos);
        }

        return restListUsersImportDTO;
    }
}
