package com.carhub.carrental.user.persistence.repository;

import java.util.UUID;
import java.time.Instant;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.carhub.carrental.user.persistence.entity.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Modifying
    @Transactional
    @Query("""
            insert into User (identityId, email, createdAt, emailVerified, updatedAt)
            values (:identityId, :email, :createdAt, false, instant)
            on conflict (identityId) do nothing
            """)
    int insertUserIfAbsent(
            @Param("email") String email,
            @Param("identityId") UUID identityId,
            @Param("createdAt") Instant createdAt
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query("""
            update User u
            set u.emailVerified = true, u.updatedAt = instant
            where u.identityId = :identityId and u.emailVerified = false
            """)
    int markEmailVerified(@Param("identityId") UUID identityId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query("""
            insert into User (identityId, email, createdAt, emailVerified, updatedAt)
            values (:identityId, :email, :createdAt, true, instant)
            on conflict (identityId) do update
            set emailVerified = true, updatedAt = instant
            where emailVerified = false
            """)
    void upsertVerifiedUser(
            @Param("email") String email,
            @Param("identityId") UUID identityId,
            @Param("createdAt") Instant createdAt
    );

    boolean existsByIdentityId(UUID identityId);
}
