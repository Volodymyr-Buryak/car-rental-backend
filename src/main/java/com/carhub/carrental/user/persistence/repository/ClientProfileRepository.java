package com.carhub.carrental.user.persistence.repository;

import java.util.UUID;
import java.util.Optional;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.carhub.carrental.user.persistence.entity.ClientProfile;

@Repository
public interface ClientProfileRepository extends JpaRepository<ClientProfile, UUID> {

    @Modifying
    @Transactional
    @Query(value = """
            insert into user_module.client_profiles (user_id)
            select u.id
            from user_module.users u
            where u.identity_out_ref = :identityId
            on conflict (user_id) do nothing
            """, nativeQuery = true)
    int insertClientProfileIfNotExists(@Param("identityId") UUID identityId);


    @EntityGraph(attributePaths = {"user", "identityDocument", "driverLicense"})
    Optional<ClientProfile> findByUser_IdentityId(UUID userIdentityId);
}
