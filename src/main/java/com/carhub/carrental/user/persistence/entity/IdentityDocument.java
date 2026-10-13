package com.carhub.carrental.user.persistence.entity;

import lombok.*;
import java.util.UUID;
import java.util.Objects;
import java.time.LocalDate;
import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.annotations.JdbcTypeCode;
import com.carhub.carrental.user.persistence.entity.enums.IdentityDocumentType;

@Getter
@Entity
@Table(name = "identity_documents", schema = "user_module")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdentityDocument {
    @Id
    @Column(name = "client_id")
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id")
    private ClientProfile client;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "user_module.identity_document_type")
    private IdentityDocumentType type;

    private String series;
    private String number;
    private LocalDate expiresAt;

    @Builder
    private IdentityDocument(
            ClientProfile client,
            IdentityDocumentType type,
            String series,
            String number,
            LocalDate expiresAt
    ) {
        this.client = client;
        this.type = type;
        this.series = series;
        this.number = number;
        this.expiresAt = expiresAt;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdentityDocument identityDocument)) return false;
        if (getEffectiveClass(this) != getEffectiveClass(identityDocument)) return false;
        return id != null && Objects.equals(id, identityDocument.id);
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
