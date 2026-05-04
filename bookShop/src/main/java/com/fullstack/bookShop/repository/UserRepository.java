package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // check exists
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // find by
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndRefreshToken(String email, String refreshToken);

    @Query("SELECT u.email FROM User u WHERE u.email IN :emails")
    Set<String> findExistingEmails(Set<String> emails);

    @Query("SELECT u.phone FROM User u WHERE u.phone IN :phones")
    Set<String> findExistingPhones(Set<String> phones);

    // delete by role
    void deleteByRoleId(Long roleId);
}
