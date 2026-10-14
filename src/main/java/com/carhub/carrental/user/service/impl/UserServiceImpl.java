package com.carhub.carrental.user.service.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.carhub.carrental.user.service.UserService;
import com.carhub.carrental.user.persistence.repository.UserRepository;
import com.carhub.carrental.user.service.exception.UserNotFoundException;
import com.carhub.carrental.user.service.exception.IdentityEmailMismatchException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void verifyEmail(UUID identityId, String email) {
        if (userRepository.markEmailVerified(identityId, email) > 0
                || userRepository.existsByIdentityIdAndEmailAndEmailVerifiedTrue(identityId, email)) {
            return;
        }
        if (!userRepository.existsByIdentityId(identityId)) {
            throw new UserNotFoundException(identityId);
        }
        throw new IdentityEmailMismatchException(identityId);
    }

}
