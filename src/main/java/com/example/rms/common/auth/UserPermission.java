package com.example.rms.common.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.rms.service.event.CustomerSyncEvent;
import com.example.rms.service.exception.UnauthorizedAccess;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.UUID;

import static com.example.rms.common.util.ContextUtil.setCustomerId;

@Aspect
@Component
public class UserPermission {
    private final HttpServletRequest request;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public UserPermission(HttpServletRequest request, ApplicationEventPublisher eventPublisher) {
        this.request = request;
        this.eventPublisher = eventPublisher;
    }

    @Before("@annotation(RequireUser)")
    public void authorizeReader(JoinPoint joinPoint) {
        try {
            var encodedToken = request.getHeader("Authorization");
            DecodedJWT jwt = JWT.decode(encodedToken);
            UUID id = UUID.fromString(jwt.getClaim("sub").asString());
            String name = jwt.getClaim("name").asString();
            eventPublisher.publishEvent(new CustomerSyncEvent(this, id, name));
            setCustomerId(id);
        } catch (Exception ex) {
            throw new UnauthorizedAccess();
        }
    }
}
