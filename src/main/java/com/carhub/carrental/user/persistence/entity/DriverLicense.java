package com.carhub.carrental.user.persistence.entity;

import lombok.*;
import java.util.UUID;
import java.util.Objects;
import java.time.LocalDate;
import jakarta.persistence.*;
import org.hibernate.proxy.HibernateProxy;

@Getter
@Entity
@Table(name = "driver_licenses", schema = "user_module")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DriverLicense {
    @Id
    @Column(name = "client_id")
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id")
    private ClientProfile client;

    private String series;
    private String number;
    private LocalDate issuedAt;
    private LocalDate expiresAt;
    private LocalDate categoryBOpenedAt;

    @Builder
    private DriverLicense(
            String series,
            String number,
            LocalDate issuedAt,
            LocalDate expiresAt,
            LocalDate categoryBOpenedAt
    ) {
        this.series = series;
        this.number = number;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.categoryBOpenedAt = categoryBOpenedAt;
    }

    void assignTo(ClientProfile client) {
        this.client = client;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DriverLicense driverLicense)) return false;
        if (getEffectiveClass(this) != getEffectiveClass(driverLicense)) return false;
        return id != null && Objects.equals(id, driverLicense.id);
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
