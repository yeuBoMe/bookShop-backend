package com.fullstack.bookShop.config;

import com.fullstack.bookShop.model.Permission;
import com.fullstack.bookShop.model.Role;
import com.fullstack.bookShop.model.User;
import com.fullstack.bookShop.repository.PermissionRepository;
import com.fullstack.bookShop.repository.RoleRepository;
import com.fullstack.bookShop.repository.UserRepository;
import com.fullstack.bookShop.util.constant.auth.AuthorityConstant;
import com.fullstack.bookShop.util.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Value("${bookshop.admin.email:}")
    private String adminEmail;

    @Value("${bookshop.admin.password:}")
    private String adminPassword;

    @Value("${bookshop.admin.full-name:Administrator}")
    private String adminFullName;

    private final PasswordEncoder passwordEncoder;

    private final PermissionRepository permissionRepository;

    private final RoleRepository roleRepository;

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        LOGGER.info(">>> START INIT DATABASE");

        long countPermissions = permissionRepository.count();

        if (countPermissions == 0) {
            ArrayList<Permission> permissions = new ArrayList<>();

            // BOOKS
            permissions.add(new Permission("Create book", "/api/v1/books", "POST", "BOOKS"));
            permissions.add(new Permission("Get list books", "/api/v1/books", "GET", "BOOKS"));
            permissions.add(new Permission("Get books by average rating", "/api/v1/books/average-rating", "GET", "BOOKS"));
            permissions.add(new Permission("Get book by id", "/api/v1/books/{id}", "GET", "BOOKS"));
            permissions.add(new Permission("Update book", "/api/v1/books/{id}", "PUT", "BOOKS"));
            permissions.add(new Permission("Partial update book", "/api/v1/books/{id}", "PATCH", "BOOKS"));
            permissions.add(new Permission("Delete book", "/api/v1/books/{id}", "DELETE", "BOOKS"));
            permissions.add(new Permission("Update book single image", "/api/v1/books/{id}/image", "PATCH", "BOOKS"));
            permissions.add(new Permission("Update book image to Cloudinary", "/api/v1/books/{id}/cloud-image", "PATCH", "BOOKS"));
            permissions.add(new Permission("Update book multiple images", "/api/v1/books/{id}/multiple-images", "PATCH", "BOOKS"));
            permissions.add(new Permission("Update book multiple images to Cloudinary", "/api/v1/books/{id}/cloud-multiple-images", "PATCH", "BOOKS"));
            permissions.add(new Permission("Delete book single image", "/api/v1/books/{id}/image", "DELETE", "BOOKS"));
            permissions.add(new Permission("Delete book image in Cloudinary", "/api/v1/books/{id}/cloud-image", "DELETE", "BOOKS"));
            permissions.add(new Permission("Delete book multiple images", "/api/v1/books/{id}/multiple-images", "DELETE", "BOOKS"));
            permissions.add(new Permission("Delete book multiple images in Cloudinary", "/api/v1/books/{id}/cloud-multiple-images", "DELETE", "BOOKS"));
            permissions.add(new Permission("Import books from file", "/api/v1/books/import", "POST", "BOOKS"));

            // CATEGORIES
            permissions.add(new Permission("Create category", "/api/v1/categories", "POST", "CATEGORIES"));
            permissions.add(new Permission("Get list categories", "/api/v1/categories", "GET", "CATEGORIES"));
            permissions.add(new Permission("Get category by id", "/api/v1/categories/{id}", "GET", "CATEGORIES"));
            permissions.add(new Permission("Update category", "/api/v1/categories/{id}", "PUT", "CATEGORIES"));
            permissions.add(new Permission("Partial update category", "/api/v1/categories/{id}", "PATCH", "CATEGORIES"));
            permissions.add(new Permission("Delete category", "/api/v1/categories/{id}", "DELETE", "CATEGORIES"));
            permissions.add(new Permission("Import categories", "/api/v1/categories/import", "POST", "CATEGORIES"));

            // ORDERS
            permissions.add(new Permission("Create order", "/api/v1/orders", "POST", "ORDERS"));
            permissions.add(new Permission("Get list orders", "/api/v1/orders", "GET", "ORDERS"));
            permissions.add(new Permission("Get order by id", "/api/v1/orders/{id}", "GET", "ORDERS"));
            permissions.add(new Permission("Partial update order", "/api/v1/orders/{id}", "PATCH", "ORDERS"));
            permissions.add(new Permission("Delete order", "/api/v1/orders/{id}", "DELETE", "ORDERS"));
            permissions.add(new Permission("Get order history", "/api/v1/orders/history", "GET", "ORDERS"));

            // RATINGS
            permissions.add(new Permission("Create rating", "/api/v1/ratings", "POST", "RATINGS"));
            permissions.add(new Permission("Get list ratings", "/api/v1/ratings", "GET", "RATINGS"));
            permissions.add(new Permission("Get ratings by book", "/api/v1/ratings/book/{bookId}", "GET", "RATINGS"));
            permissions.add(new Permission("Get rating by user and book", "/api/v1/ratings/book/{bookId}/user/{userId}", "GET", "RATINGS"));
            permissions.add(new Permission("Get rating by id", "/api/v1/ratings/{id}", "GET", "RATINGS"));
            permissions.add(new Permission("Delete rating", "/api/v1/ratings/{id}", "DELETE", "RATINGS"));

            // PERMISSIONS
            permissions.add(new Permission("Create permission", "/api/v1/permissions", "POST", "PERMISSIONS"));
            permissions.add(new Permission("Get list permissions", "/api/v1/permissions", "GET", "PERMISSIONS"));
            permissions.add(new Permission("Get permission by id", "/api/v1/permissions/{id}", "GET", "PERMISSIONS"));
            permissions.add(new Permission("Update permission", "/api/v1/permissions/{id}", "PUT", "PERMISSIONS"));
            permissions.add(new Permission("Delete permission", "/api/v1/permissions/{id}", "DELETE", "PERMISSIONS"));

            // ROLES
            permissions.add(new Permission("Create role", "/api/v1/roles", "POST", "ROLES"));
            permissions.add(new Permission("Get list roles", "/api/v1/roles", "GET", "ROLES"));
            permissions.add(new Permission("Get role by id", "/api/v1/roles/{id}", "GET", "ROLES"));
            permissions.add(new Permission("Update role", "/api/v1/roles/{id}", "PUT", "ROLES"));
            permissions.add(new Permission("Delete role", "/api/v1/roles/{id}", "DELETE", "ROLES"));

            // USERS
            permissions.add(new Permission("Create user", "/api/v1/users", "POST", "USERS"));
            permissions.add(new Permission("Get list users", "/api/v1/users", "GET", "USERS"));
            permissions.add(new Permission("Get user by id", "/api/v1/users/{id}", "GET", "USERS"));
            permissions.add(new Permission("Update user", "/api/v1/users/{id}", "PUT", "USERS"));
            permissions.add(new Permission("Partial update user", "/api/v1/users/{id}", "PATCH", "USERS"));
            permissions.add(new Permission("Update user password", "/api/v1/users/{id}/password", "PATCH", "USERS"));
            permissions.add(new Permission("Delete user", "/api/v1/users/{id}", "DELETE", "USERS"));
            permissions.add(new Permission("Update user avatar", "/api/v1/users/{id}/avatar", "PATCH", "USERS"));
            permissions.add(new Permission("Update user avatar to Cloudinary", "/api/v1/users/{id}/cloud-avatar", "PATCH", "USERS"));
            permissions.add(new Permission("Delete user avatar", "/api/v1/users/{id}/avatar", "DELETE", "USERS"));
            permissions.add(new Permission("Delete user avatar in Cloudinary", "/api/v1/users/{id}/cloud-avatar", "DELETE", "USERS"));
            permissions.add(new Permission("Import users from file", "/api/v1/users/import", "POST", "USERS"));

            // VNPAY
            permissions.add(new Permission("Create VNPay payment URL", "/api/v1/vnpay/create-url", "POST", "PAYMENTS"));

            // MOMO
            permissions.add(new Permission("Create MoMo payment URL", "/api/v1/momo/create-url", "POST", "PAYMENTS"));

            // FILE
            permissions.add(new Permission("Upload single file", "/api/v1/files/single", "POST", "FILES"));
            permissions.add(new Permission("Upload multiple files", "/api/v1/files/multiple", "POST", "FILES"));

            // DASHBOARD
            permissions.add(new Permission("Get dashboard information", "/api/v1/dashboard", "GET", "DASHBOARD"));

            // CLOUDINARY
            permissions.add(new Permission("Upload image to Cloudinary", "/api/v1/cloudinary/upload", "POST", "CLOUDINARY"));

            // CHATBOT
            permissions.add(new Permission("Chat with AI", "/api/v1/chatbot", "POST", "CHATBOT"));

            permissionRepository.saveAll(permissions);
        }

        List<Permission> allPermissions = this.permissionRepository.findAll();

        Role adminRole = findOrCreateRole(AuthorityConstant.ADMIN, "Toan quyen quan tri he thong");
        findOrCreateRole(AuthorityConstant.USER, "Nguoi dung cuoi");

        syncPermissionsForAdminRole(adminRole, allPermissions);
        createAdminUserIfConfigured(adminRole);

        LOGGER.info(
                ">>> END INIT DATABASE (permissions={}, seeded={})",
                allPermissions.size(),
                countPermissions == 0
        );
    }

    /** Role phai duoc seed o day: DB moi khong co san role nao, thieu ADMIN la app khong khoi dong duoc. */
    private Role findOrCreateRole(String name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    role.setDescription(description);
                    role.setActive(true);
                    LOGGER.info(">>> Create missing role: {}", name);
                    return roleRepository.save(role);
                });
    }

    /** Chi ghi khi thuc su thieu quyen, tranh update bang role_permission moi lan khoi dong. */
    private void syncPermissionsForAdminRole(Role adminRole, List<Permission> allPermissions) {
        int currentSize = adminRole.getPermissions() != null ? adminRole.getPermissions().size() : 0;
        if (currentSize == allPermissions.size()) {
            return;
        }

        adminRole.setPermissions(allPermissions);
        roleRepository.save(adminRole);
        LOGGER.info(">>> Sync permissions for role ADMIN: {} -> {}", currentSize, allPermissions.size());
    }

    /** Tai khoan admin lay tu bien moi truong. Khong cau hinh thi bo qua, KHONG chan khoi dong. */
    private void createAdminUserIfConfigured(Role adminRole) {
        String email = StringUtils.trimLowerBlank(adminEmail);
        if (email == null || StringUtils.isNullOrBlank(adminPassword)) {
            LOGGER.info(">>> Skip admin seeding: chua cau hinh bookshop.admin.email / bookshop.admin.password");
            return;
        }

        if (userRepository.existsByEmail(email)) {
            LOGGER.info(">>> Admin user already exists: {}", email);
            return;
        }

        User admin = new User();
        admin.setEmail(email);
        admin.setFullName(StringUtils.normalizeSpaces(adminFullName));
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(adminRole);

        userRepository.save(admin);
        LOGGER.info(">>> Create admin user: {}", email);
    }
}
