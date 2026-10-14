package com.carhub.carrental.user.persistence.repository;

import java.util.UUID;
import java.time.Instant;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import com.carhub.carrental.user.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Modifying
    @Transactional
    @Query("""
            insert into User (identityId, email, createdAt)
            values (:identityId, :email, :createdAt)
            on conflict (identityId) do nothing
            """)
    void insertUserIfNotExists(
            @Param("email") String email,
            @Param("identityId") UUID identityId,
            @Param("createdAt") Instant createdAt
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query("""
            update User u
            set u.emailVerified = true
            where u.identityId = :identityId and u.email = :email and u.emailVerified = false
            """)
    int markEmailVerified(@Param("identityId") UUID identityId, @Param("email") String email);

    boolean existsByIdentityId(UUID identityId);
    boolean existsByIdentityIdAndEmailAndEmailVerifiedTrue(UUID identityId, String email);
}
