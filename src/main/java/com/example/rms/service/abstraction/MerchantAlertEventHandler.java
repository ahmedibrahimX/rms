package com.example.rms.service.abstraction;

import com.example.rms.service.event.abstraction.MerchantAlertEvent;

public interface MerchantAlertEventHandler<T extends MerchantAlertEvent> extends CustomEventHandler<T> {
    void handle(T event);
}
