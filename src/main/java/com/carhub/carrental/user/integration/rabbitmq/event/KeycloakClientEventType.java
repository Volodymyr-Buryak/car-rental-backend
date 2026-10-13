package com.carhub.carrental.user.integration.rabbitmq.event;

public enum KeycloakClientEventType {
    REGISTER,
    VERIFY_EMAIL,
    UPDATE_EMAIL,
    DELETE_ACCOUNT
}