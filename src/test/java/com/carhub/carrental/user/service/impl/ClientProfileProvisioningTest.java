package com.carhub.carrental.user.service.impl;

import com.carhub.carrental.user.persistence.mapper.ClientProfileMapper;
import com.carhub.carrental.user.persistence.mapper.UserMapper;
import com.carhub.carrental.user.service.impl.completion.ProfileCompletionEvaluator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.*;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import com.carhub.carrental.user.persistence.entity.*;
import com.carhub.carrental.user.persistence.entity.enums.UserStatus;
import com.carhub.carrental.user.persistence.repository.ClientProfileRepository;
import com.carhub.carrental.user.persistence.repository.UserRepository;
import com.carhub.carrental.user.service.ClientProfileService;
import com.carhub.carrental.user.service.UserService;
import com.carhub.carrental.user.service.exception.user.UserNotFoundException;
import com.carhub.carrental.user.service.exception.user.IdentityEmailMismatchException;

class ClientProfileProvisioningTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
    private static final TimeZone ORIGINAL_TIME_ZONE = TimeZone.getDefault();
    private static final GenericContainer<?> POSTGRES = new GenericContainer<>("postgres:18.6")
            .withEnv("POSTGRES_DB", "provisioning_test").withEnv("POSTGRES_USER", "test")
            .withEnv("POSTGRES_PASSWORD", "test").withExposedPorts(5432)
            .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2));
    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;
    private EntityManager entityManager;
    private UserRepository users;
    private ClientProfileRepository profiles;
    private JpaTransactionManager transactionManager;
    private ClientProfileService service;

    @BeforeAll
    static void startDatabase() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        POSTGRES.start();
        String url = "jdbc:postgresql://" + POSTGRES.getHost() + ":" + POSTGRES.getMappedPort(5432)
                + "/provisioning_test";
        try (var connection = DriverManager.getConnection(url, "test", "test");
             var statement = connection.createStatement();
             var migration = ClientProfileProvisioningTest.class.getResourceAsStream(
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
                .addAnnotatedClass(ClientProfile.class).addAnnotatedClass(IdentityDocument.class)
                .addAnnotatedClass(DriverLicense.class).buildMetadata().buildSessionFactory();
    }

    @AfterAll
    static void stopDatabase() {
        if (sessionFactory != null) sessionFactory.close();
        if (registry != null) StandardServiceRegistryBuilder.destroy(registry);
        POSTGRES.stop();
        TimeZone.setDefault(ORIGINAL_TIME_ZONE);
    }

    @BeforeEach
    void createService() {
        entityManager = SharedEntityManagerCreator.createSharedEntityManager(sessionFactory);
        var factory = new JpaRepositoryFactory(entityManager);
        users = factory.getRepository(UserRepository.class);
        profiles = factory.getRepository(ClientProfileRepository.class);
        transactionManager = new JpaTransactionManager(sessionFactory);
        service = transactionalService(profiles);
    }

    @Test
    void provisioningCreatesUserAndProfileWithDatabaseDefaults() {
        UUID identityId = UUID.randomUUID();
        String email = identityId + "@example.com";
        service.provisionClient(identityId, email, CREATED_AT);

        User user = findUser(identityId).orElseThrow();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.isEmailVerified()).isFalse();
        assertThat(profiles.existsById(user.getId())).isTrue();
    }

    @Test
    void duplicateDeliveryPreservesExistingUserAndProfileData() {
        UUID identityId = UUID.randomUUID();
        String email = identityId + "@example.com";
        service.provisionClient(identityId, email, CREATED_AT);
        User first = findUser(identityId).orElseThrow();
        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                profiles.findById(first.getId()).orElseThrow().setBirthDate(birthDate));

        service.provisionClient(identityId, identityId + "-changed@example.com", CREATED_AT.plusSeconds(60));

        User repeated = findUser(identityId).orElseThrow();
        assertThat(repeated.getId()).isEqualTo(first.getId());
        assertThat(repeated.getEmail()).isEqualTo(email);
        assertThat(repeated.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(profiles.findById(first.getId()).orElseThrow().getBirthDate()).isEqualTo(birthDate);
    }

    @Test
    void concurrentDeliveriesCreateOnlyOneUserAndProfile() throws Exception {
        UUID identityId = UUID.randomUUID();
        var barrier = new CyclicBarrier(2);
        Callable<Void> provisioning = () -> {
            barrier.await(10, TimeUnit.SECONDS);
            service.provisionClient(identityId, identityId + "@example.com", CREATED_AT);
            return null;
        };
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var results = executor.invokeAll(List.of(provisioning, provisioning));
            for (var result : results) result.get(30, TimeUnit.SECONDS);
        }

        User user = findUser(identityId).orElseThrow();
        assertThat(profiles.existsById(user.getId())).isTrue();
        assertThat(entityManager.createQuery("select count(u) from User u where u.identityId = :identityId", Long.class)
                .setParameter("identityId", identityId).getSingleResult()).isEqualTo(1L);
    }

    @Test
    void profileInsertFailureRollsBackUserCreation() {
        UUID identityId = UUID.randomUUID();
        var failure = new IllegalStateException("profile insert failed");
        var failingProfiles = mock(ClientProfileRepository.class);
        when(failingProfiles.insertClientProfileIfNotExists(identityId)).thenThrow(failure);

        assertThatThrownBy(() -> transactionalService(failingProfiles)
                .provisionClient(identityId, identityId + "@example.com", CREATED_AT)).isSameAs(failure);
        assertThat(findUser(identityId)).isEmpty();
    }

    private ClientProfileService transactionalService(ClientProfileRepository profileRepository) {
        var factory = new ProxyFactory(new ClientProfileServiceImpl(mock(UserMapper.class), users,
                mock(ClientProfileMapper.class), mock(ProfileCompletionEvaluator.class), profileRepository));
        factory.addAdvice(new TransactionInterceptor(transactionManager, new AnnotationTransactionAttributeSource()));
        return (ClientProfileService) factory.getProxy();
    }

    @Test
    void verificationBeforeRegistrationRetriesWithoutCreatingUser() {
        UUID identityId = UUID.randomUUID();
        String email = identityId + "@example.com";
        var verification = verificationService();
        assertThatThrownBy(() -> verification.verifyEmail(identityId, email))
                .isInstanceOf(UserNotFoundException.class);
        assertThat(findUser(identityId)).isEmpty();
        service.provisionClient(identityId, email, CREATED_AT);
        verification.verifyEmail(identityId, email);

        User registered = findUser(identityId).orElseThrow();
        assertThat(registered.isEmailVerified()).isTrue();
        assertThat(registered.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(profiles.existsById(registered.getId())).isTrue();
    }

    @Test
    void mismatchingEmailDoesNotConfirmOrOverwriteExistingAddress() {
        UUID identityId = UUID.randomUUID();
        String originalEmail = identityId + "@example.com";
        service.provisionClient(identityId, originalEmail, CREATED_AT);

        assertThatThrownBy(() -> verificationService()
                .verifyEmail(identityId, identityId + "-new@example.com"))
                .isInstanceOf(IdentityEmailMismatchException.class);

        User user = findUser(identityId).orElseThrow();
        assertThat(user.isEmailVerified()).isFalse();
        assertThat(user.getEmail()).isEqualTo(originalEmail);
    }

    @Test
    void repeatedVerificationDoesNotChangeUpdateTimestamp() {
        UUID identityId = UUID.randomUUID();
        String email = identityId + "@example.com";
        service.provisionClient(identityId, email, CREATED_AT);
        var verification = verificationService();
        verification.verifyEmail(identityId, email);
        Instant updatedAt = findUser(identityId).orElseThrow().getUpdatedAt();

        verification.verifyEmail(identityId, email);

        assertThat(findUser(identityId).orElseThrow().getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void concurrentVerificationsConfirmExistingUser() throws Exception {
        UUID identityId = UUID.randomUUID();
        String email = identityId + "@example.com";
        service.provisionClient(identityId, email, CREATED_AT);
        var verification = verificationService();
        var barrier = new CyclicBarrier(2);
        Callable<Void> task = () -> {
            barrier.await(10, TimeUnit.SECONDS);
            verification.verifyEmail(identityId, email);
            return null;
        };
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var results = executor.invokeAll(List.of(task, task));
            for (var result : results) result.get(30, TimeUnit.SECONDS);
        }

        User user = findUser(identityId).orElseThrow();
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(profiles.existsById(user.getId())).isTrue();
        assertThat(entityManager.createQuery("select count(u) from User u where u.identityId = :identityId", Long.class)
                .setParameter("identityId", identityId).getSingleResult()).isEqualTo(1L);
    }

    private UserService verificationService() {
        var factory = new ProxyFactory(new UserServiceImpl(users));
        factory.addAdvice(new TransactionInterceptor(transactionManager, new AnnotationTransactionAttributeSource()));
        return (UserService) factory.getProxy();
    }

    private Optional<User> findUser(UUID identityId) {
        return entityManager.createQuery("from User u where u.identityId = :identityId", User.class)
                .setParameter("identityId", identityId).getResultList().stream().findFirst();
    }
}
