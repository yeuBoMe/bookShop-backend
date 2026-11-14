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
    @Query("""
        SELECT
            COUNT(r) > 0
        FROM Role r
        WHERE LOWER(r.name) = LOWER(:name)
    """)
    boolean existsByNameIgnoreCase(@Param("name") String name);

    // check set<name> exist
    @Query("""
        SELECT
            LOWER(r.name)
        FROM Role r
        WHERE LOWER(r.name) IN :names
    """)
    Set<String> findExistingNamesIgnoreCase(@Param("names") Set<String> names);

    Optional<Role> findByName(String name);

    /*
     * RoleMapper doc role.permissions de dung RoleResponse, ma controller map
     * SAU khi da ra khoi service. Voi open-in-view=false, Session da dong nen
     * doc lazy o do se nem LazyInitializationException -> nap san bang fetch join.
     */
    @Query("""
        SELECT
            r
        FROM Role r
        LEFT JOIN FETCH r.permissions
        WHERE r.id = :id
    """)
    Optional<Role> findByIdFetchPermissions(@Param("id") Long id);
}
