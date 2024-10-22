package com.example.rms.service.event.abstraction;

import java.util.UUID;

public interface MerchantAlertEvent extends CustomEvent {
    UUID branchId();
}
