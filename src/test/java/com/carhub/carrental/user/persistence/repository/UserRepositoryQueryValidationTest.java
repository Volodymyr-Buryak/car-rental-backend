package com.carhub.carrental.user.persistence.repository;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import com.carhub.carrental.user.persistence.entity.*;

class UserRepositoryQueryValidationTest {

    @Test
    void repositoryQueriesAreValidForCurrentHibernateMappings() {
        var registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                .applySetting("hibernate.boot.allow_jdbc_metadata_access", false)
                .applySetting("hibernate.physical_naming_strategy",
                        "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy")
                .build();
        try (var factory = new MetadataSources(registry)
                .addAnnotatedClass(User.class).addAnnotatedClass(ClientProfile.class)
                .addAnnotatedClass(IdentityDocument.class).addAnnotatedClass(DriverLicense.class)
                .buildMetadata().buildSessionFactory();
             var entityManager = factory.createEntityManager()) {
            assertThatCode(() -> new JpaRepositoryFactory(entityManager).getRepository(UserRepository.class))
                    .doesNotThrowAnyException();
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
