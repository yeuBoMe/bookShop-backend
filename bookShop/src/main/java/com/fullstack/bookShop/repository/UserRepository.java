package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // check exists
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    // find by
    User findByEmail(String email);
    User findByEmailAndRefreshToken(String email, String refreshToken);

    // delete by
    void deleteByRoleId(Long roleId);
}
