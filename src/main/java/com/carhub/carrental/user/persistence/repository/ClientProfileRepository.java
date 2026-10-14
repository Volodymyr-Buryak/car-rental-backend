package com.carhub.carrental.user.persistence.repository;

import java.util.UUID;
import jakarta.transaction.Transactional;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import com.carhub.carrental.user.persistence.entity.ClientProfile;

@Repository
public interface ClientProfileRepository extends JpaRepository<ClientProfile, UUID> {

    @Modifying
    @Transactional
    @Query("""
            insert into ClientProfile (id)
            select u.id from User u where u.identityId = :identityId
            on conflict (id) do nothing
            """)
    int insertClientProfileIfNotExists(@Param("identityId") UUID identityId);


}
