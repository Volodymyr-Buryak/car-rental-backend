package com.carhub.carrental.user.persistence.entity;

import lombok.*;
import java.util.UUID;
import java.util.Objects;
import java.time.LocalDate;
import jakarta.persistence.*;
import org.hibernate.proxy.HibernateProxy;

@Getter
@Entity
@Table(name = "client_profiles", schema = "user_module")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClientProfile {

    @Id
    @Column(name = "user_id")
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDate birthDate;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientProfile clientProfile)) return false;
        if (getEffectiveClass(this) != getEffectiveClass(clientProfile)) return false;
        return id != null && Objects.equals(id, clientProfile.id);
    }

    @Override
    public final int hashCode() {
        return getEffectiveClass(this).hashCode();
    }

    private static Class<?> getEffectiveClass(Object object) {
        return object instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : object.getClass();
    }

}
