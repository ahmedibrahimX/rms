package com.example.rms.common.util;

import com.example.rms.service.exception.UnauthorizedAccess;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.UUID;

public class ContextUtil {
    public static void setCustomerId(UUID customerId) {
        RequestContextHolder.getRequestAttributes().setAttribute("customerId", customerId, RequestAttributes.SCOPE_REQUEST);
    }

    public static UUID getCustomerId() {
        try{
            return (UUID) RequestContextHolder.getRequestAttributes().getAttribute("customerId", RequestAttributes.SCOPE_REQUEST);
        } catch (Exception ex) {
            throw new UnauthorizedAccess();
        }
    }
}
