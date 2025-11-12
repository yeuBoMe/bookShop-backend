package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // check exists
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // find by
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndRefreshToken(String email, String refreshToken);

    // delete by role
    void deleteByRoleId(Long roleId);
}
