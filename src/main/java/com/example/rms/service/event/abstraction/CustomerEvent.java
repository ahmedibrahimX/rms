package com.example.rms.service.event.abstraction;

import java.util.UUID;

public interface CustomerEvent extends CustomEvent {
    UUID customerId();
}
