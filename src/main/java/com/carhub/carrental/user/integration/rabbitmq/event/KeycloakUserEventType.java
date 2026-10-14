package com.carhub.carrental.user.integration.rabbitmq.event;

public enum KeycloakUserEventType {
    REGISTER,
    VERIFY_EMAIL,
    UPDATE_EMAIL,
    DELETE_ACCOUNT
}