package com.carhub.carrental.user.aop;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.aspectj.lang.annotation.AfterReturning;
import org.springframework.core.annotation.Order;

@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingAspect {

    @AfterReturning(pointcut = """
            execution(* com.carhub.carrental.user..UserServiceImpl.verifyEmail(..))
            && args(identityId, ..)
            """
    )
    public void logEmailVerification(UUID identityId) {
        log.info("Email verification processed successfully: userId={}", identityId);
    }

    @AfterReturning(pointcut = """
            execution(* com.carhub.carrental.user..ClientProfileServiceImpl.provisionClient(..)) 
            && args(identityId, ..)
            """
    )
    public void logClientProvisioning(UUID identityId) {
        log.info("Client provisioning completed successfully: userId={}", identityId);
    }

}
