package com.fullstack.bookShop.repository;

import com.fullstack.bookShop.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    // check name exists
    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE LOWER(r.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    // check set<name> exist
    @Query("SELECT LOWER(r.name) FROM Role r WHERE LOWER(r.name) IN :names")
    Set<String> findExistingNamesIgnoreCase(@Param("names") Set<String> names);

    Optional<Role> findByName(String name);
}
