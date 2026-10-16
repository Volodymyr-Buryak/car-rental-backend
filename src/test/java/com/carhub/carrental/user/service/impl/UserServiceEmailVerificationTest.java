package com.carhub.carrental.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import com.carhub.carrental.user.integration.rabbitmq.config.KeycloakRabbitMqConfig;
import com.carhub.carrental.user.persistence.repository.UserRepository;
import com.carhub.carrental.user.service.UserService;
import com.carhub.carrental.user.service.exception.user.IdentityEmailMismatchException;
import com.carhub.carrental.user.service.exception.user.UserNotFoundException;

class UserServiceEmailVerificationTest {

    private static final UUID IDENTITY_ID = UUID.randomUUID();
    private static final String EMAIL = "user@example.com";
    private final UserRepository users = mock(UserRepository.class);
    private final PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
    private final SimpleTransactionStatus status = new SimpleTransactionStatus();
    private final UserService service = transactionalService();

    @Test
    void matchingEmailIsConfirmedAndCommitted() {
        when(users.markEmailVerified(IDENTITY_ID, EMAIL)).thenReturn(1);
        service.verifyEmail(IDENTITY_ID, EMAIL);
        var order = inOrder(users, transactions);
        order.verify(transactions).getTransaction(any());
        order.verify(users).markEmailVerified(IDENTITY_ID, EMAIL);
        order.verify(transactions).commit(status);
        verifyNoMoreInteractions(users);
    }

    @Test
    void alreadyVerifiedMatchingEmailIsSuccessful() {
        when(users.existsByIdentityIdAndEmailAndEmailVerifiedTrue(IDENTITY_ID, EMAIL)).thenReturn(true);
        service.verifyEmail(IDENTITY_ID, EMAIL);
        verify(transactions).commit(status);
        verify(users, never()).existsByIdentityId(any());
        verify(users, never()).insertUserIfNotExists(any(), any(), any());
    }

    @Test
    void missingLocalUserRollsBackAndIsRetryableWithoutCreatingUser() {
        assertThatThrownBy(() -> service.verifyEmail(IDENTITY_ID, EMAIL))
                .isInstanceOf(UserNotFoundException.class);
        verify(transactions).rollback(status);
        verify(transactions, never()).commit(any());
        verify(users, never()).insertUserIfNotExists(any(), any(), any());
        assertThat(new KeycloakRabbitMqConfig(null).keycloakRetryableExceptionClassifier()
                .isRetryable(new UserNotFoundException(IDENTITY_ID))).isTrue();
    }

    @Test
    void retrySucceedsAfterRegistrationCreatesUser() {
        assertThatThrownBy(() -> service.verifyEmail(IDENTITY_ID, EMAIL))
                .isInstanceOf(UserNotFoundException.class);
        when(users.markEmailVerified(IDENTITY_ID, EMAIL)).thenReturn(1);
        service.verifyEmail(IDENTITY_ID, EMAIL);
        verify(transactions).commit(status);
    }

    @Test
    void mismatchingEmailDoesNotConfirmAnotherAddress() {
        when(users.existsByIdentityId(IDENTITY_ID)).thenReturn(true);
        assertThatThrownBy(() -> service.verifyEmail(IDENTITY_ID, EMAIL))
                .isInstanceOf(IdentityEmailMismatchException.class);
        verify(transactions).rollback(status);
        assertThat(new KeycloakRabbitMqConfig(null).keycloakRetryableExceptionClassifier()
                .isRetryable(new IdentityEmailMismatchException(IDENTITY_ID))).isTrue();
    }

    @Test
    void databaseFailureIsPreservedAndRollsBack() {
        var failure = new DataAccessResourceFailureException("database unavailable");
        when(users.markEmailVerified(IDENTITY_ID, EMAIL)).thenThrow(failure);
        assertThatThrownBy(() -> service.verifyEmail(IDENTITY_ID, EMAIL)).isSameAs(failure);
        verify(transactions).rollback(status);
        verify(transactions, never()).commit(any());
    }

    private UserService transactionalService() {
        when(transactions.getTransaction(any())).thenReturn(status);
        var proxy = new ProxyFactory(new UserServiceImpl(users));
        proxy.addAdvice(new TransactionInterceptor(transactions, new AnnotationTransactionAttributeSource()));
        return (UserService) proxy.getProxy();
    }
}
