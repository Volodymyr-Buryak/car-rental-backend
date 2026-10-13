package com.carhub.carrental.user.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.time.Instant;
import java.util.TimeZone;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.*;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import com.carhub.carrental.user.persistence.entity.User;
import com.carhub.carrental.user.persistence.entity.enums.UserStatus;

class UserRepositoryTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
    private static final TimeZone ORIGINAL_TIME_ZONE = TimeZone.getDefault();
    private static final GenericContainer<?> POSTGRES = new GenericContainer<>("postgres:18.6")
            .withEnv("POSTGRES_DB", "repository_test")
            .withEnv("POSTGRES_USER", "test")
            .withEnv("POSTGRES_PASSWORD", "test")
            .withExposedPorts(5432)
            .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2));

    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;
    private EntityManager entityManager;
    private UserRepository repository;

    @BeforeAll
    static void startDatabase() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        POSTGRES.start();
        String url = "jdbc:postgresql://" + POSTGRES.getHost() + ":" + POSTGRES.getMappedPort(5432)
                + "/repository_test";
        try (var connection = DriverManager.getConnection(url, "test", "test");
             var statement = connection.createStatement();
             var migration = UserRepositoryTest.class.getResourceAsStream(
                     "/db/migration/V1791044379__init_user_schema.sql")) {
            statement.execute(new String(migration.readAllBytes(), StandardCharsets.UTF_8));
        }
        registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.url", url)
                .applySetting("hibernate.connection.username", "test")
                .applySetting("hibernate.connection.password", "test")
                .applySetting("hibernate.hbm2ddl.auto", "none")
                .applySetting("hibernate.physical_naming_strategy",
                        "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy")
                .build();
        sessionFactory = new MetadataSources(registry).addAnnotatedClass(User.class)
                .buildMetadata().buildSessionFactory();
    }

    @AfterAll
    static void stopDatabase() {
        if (sessionFactory != null) sessionFactory.close();
        if (registry != null) StandardServiceRegistryBuilder.destroy(registry);
        POSTGRES.stop();
        TimeZone.setDefault(ORIGINAL_TIME_ZONE);
    }

    @BeforeEach
    void createRepository() {
        entityManager = sessionFactory.createEntityManager();
        entityManager.getTransaction().begin();
        repository = new JpaRepositoryFactory(entityManager).getRepository(UserRepository.class);
    }

    @AfterEach
    void rollback() {
        if (entityManager.getTransaction().isActive()) entityManager.getTransaction().rollback();
        entityManager.close();
    }

    @Test
    void registrationGeneratesIdAndDefaultsAndIgnoresDuplicates() {
        UUID identityId = UUID.randomUUID();
        assertThat(repository.insertUserIfAbsent("original@example.com", identityId, CREATED_AT)).isEqualTo(1);
        assertThat(repository.insertUserIfAbsent("changed@example.com", identityId, CREATED_AT.plusSeconds(60)))
                .isZero();
        User user = findUser(identityId);
        assertThat(user.getId()).isNotNull();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getEmailVerified()).isFalse();
        assertThat(user.getEmail()).isEqualTo("original@example.com");
        assertThat(user.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(user.getUpdatedAt()).isAfterOrEqualTo(CREATED_AT);
    }

    @Test
    void verificationRefreshesLoadedEntityAndDoesNotChangeItAgainOnDuplicate() {
        UUID identityId = UUID.randomUUID();
        assertThat(repository.markEmailVerified(identityId)).isZero();
        repository.insertUserIfAbsent("user@example.com", identityId, CREATED_AT);
        User before = findUser(identityId);
        assertThat(repository.markEmailVerified(identityId)).isEqualTo(1);
        User after = repository.findById(before.getId()).orElseThrow();
        assertThat(after.getEmailVerified()).isTrue();
        assertThat(repository.markEmailVerified(identityId)).isZero();
        assertThat(repository.findById(before.getId()).orElseThrow().getUpdatedAt()).isEqualTo(after.getUpdatedAt());
    }

    @Test
    void upsertCreatesVerifiedUserWithoutChangingItsDatesOnDuplicate() {
        UUID identityId = UUID.randomUUID();
        repository.upsertVerifiedUser("user@example.com", identityId, CREATED_AT);
        User first = findUser(identityId);
        assertThat(first.getEmailVerified()).isTrue();
        assertThat(first.getStatus()).isEqualTo(UserStatus.ACTIVE);
        repository.upsertVerifiedUser("changed@example.com", identityId, CREATED_AT.plusSeconds(60));
        User repeated = repository.findById(first.getId()).orElseThrow();
        assertThat(repeated.getEmail()).isEqualTo("user@example.com");
        assertThat(repeated.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(repeated.getUpdatedAt()).isEqualTo(first.getUpdatedAt());
    }

    @Test
    void upsertVerifiesExistingUserAndPreservesRegistrationData() {
        UUID identityId = UUID.randomUUID();
        repository.insertUserIfAbsent("original@example.com", identityId, CREATED_AT);
        User before = findUser(identityId);
        repository.upsertVerifiedUser("original@example.com", identityId, CREATED_AT.plusSeconds(60));
        User after = repository.findById(before.getId()).orElseThrow();
        assertThat(after.getEmailVerified()).isTrue();
        assertThat(after.getEmail()).isEqualTo(before.getEmail());
        assertThat(after.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void conflictingEmailForDifferentIdentityIsNotSilentlyIgnored() {
        repository.insertUserIfAbsent("user@example.com", UUID.randomUUID(), CREATED_AT);
        assertThatThrownBy(() -> repository.insertUserIfAbsent("user@example.com", UUID.randomUUID(), CREATED_AT))
                .isInstanceOf(RuntimeException.class);
    }

    private User findUser(UUID identityId) {
        return entityManager.createQuery("from User u where u.identityId = :identityId", User.class)
                .setParameter("identityId", identityId).getSingleResult();
    }
}
