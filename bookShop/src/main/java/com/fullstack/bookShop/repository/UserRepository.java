package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // Nap kem role de tranh N+1 khi map danh sach user
    @Override
    @EntityGraph(attributePaths = "role")
    Page<User> findAll(Specification<User> spec, Pageable pageable);

    // check exists
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // find by
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndRefreshToken(String email, String refreshToken);

    /*
     * Dung cho luong dang nhap / refresh / lay thong tin tai khoan.
     *
     * Cac luong nay deu phai doc role.permissions de dung LoginResponse. Voi
     * spring.jpa.open-in-view=false, Session dong ngay khi ra khoi service nen
     * doc lazy o tang tren se nem LazyInitializationException. Nap san bang
     * fetch join: dung 1 cau query thay vi 1 + N.
     */
    @Query("""
        SELECT
            u
        FROM User u
        LEFT JOIN FETCH u.role r
        LEFT JOIN FETCH r.permissions
        WHERE u.email = :email
    """)
    Optional<User> findByEmailFetchRoleAndPermissions(@Param("email") String email);

    @Query("""
        SELECT
            u
        FROM User u
        LEFT JOIN FETCH u.role r
        LEFT JOIN FETCH r.permissions
        WHERE u.email = :email
            AND u.refreshToken = :refreshToken
    """)
    Optional<User> findByEmailAndRefreshTokenFetchRoleAndPermissions(
            @Param("email") String email,
            @Param("refreshToken") String refreshToken
    );

    @Query("""
        SELECT
            u.email
        FROM User u
        WHERE u.email IN :emails
    """)
    Set<String> findExistingEmails(Set<String> emails);

    @Query("""
        SELECT
            u.phone
        FROM User u
        WHERE u.phone IN :phones
    """)
    Set<String> findExistingPhones(Set<String> phones);

    // delete by role
    void deleteByRoleId(Long roleId);
}
