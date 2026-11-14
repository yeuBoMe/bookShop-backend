package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {

    // check exists
    @Query("""
        SELECT
            COUNT(p) > 0
        FROM Permission p
        WHERE LOWER(p.name) = LOWER(:name)
    """)
    boolean existsByNameIgnoreCase(@Param("name") String name);

    boolean existsByApiPathAndMethodAndModule(String apiPath, String method, String module);

    // find by list ids
    List<Permission> findALlByIdIn(List<Long> ids);
}
