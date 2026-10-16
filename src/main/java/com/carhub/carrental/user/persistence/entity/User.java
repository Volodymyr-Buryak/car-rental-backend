package com.carhub.carrental.user.persistence.entity;

import lombok.*;
import java.util.UUID;
import java.time.Instant;
import java.util.Objects;
import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.annotations.JdbcTypeCode;
import com.carhub.carrental.user.persistence.entity.enums.UserStatus;

@Getter
@Setter
@Entity
@Table(name = "users", schema = "user_module")
@NoArgsConstructor
public class User {
    @Id
    @Setter(AccessLevel.PRIVATE)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "identity_out_ref", updatable = false)
    private UUID identityId;

    private String email;
    private String avatarUrl;
    private boolean emailVerified = false;

    private String firstName;
    private String lastName;
    private String middleName;
    private String phoneNumber;


    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "user_module.user_status")
    private UserStatus status = UserStatus.ACTIVE;

    @Column(updatable = false)
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = Instant.now();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        if (getEffectiveClass(this) != getEffectiveClass(user)) return false;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public final int hashCode() {
        return getEffectiveClass(this).hashCode();
    }

    private static Class<?> getEffectiveClass(Object object) {
        return (object instanceof HibernateProxy proxy)
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : object.getClass();
    }
}
