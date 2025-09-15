package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {

    // check exists
    boolean existsByName(String name);
    boolean existsByApiPathAndMethodAndModule(String apiPath, String method, String module);

    // find by list ids
    List<Permission> findALlByIdIn(List<Long> ids);
}
