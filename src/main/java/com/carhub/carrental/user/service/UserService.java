package com.carhub.carrental.user.service;

import java.util.UUID;

public interface UserService {
    void verifyEmail(UUID identityId);
    void register(UUID identityId, String email, long registrationTime);
}
