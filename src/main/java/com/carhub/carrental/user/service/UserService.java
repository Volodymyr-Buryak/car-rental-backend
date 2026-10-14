package com.carhub.carrental.user.service;

import java.util.UUID;

public interface UserService {
    void verifyEmail(UUID identityId, String email);
}
