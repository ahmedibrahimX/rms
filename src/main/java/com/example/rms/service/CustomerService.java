package com.example.rms.service;

import com.example.rms.infra.entity.Customer;
import com.example.rms.infra.repo.CustomerRepo;
import com.example.rms.service.event.CustomerSyncEvent;
import com.example.rms.service.model.PlacedOrderDetails;
import com.example.rms.service.model.interfaces.OrderBase;
import com.example.rms.service.pattern.decorator.RetriableStepDecorator;
import com.example.rms.service.pattern.pipeline.Pipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerService {
    private CustomerRepo customerRepo;

    @Autowired
    public CustomerService(CustomerRepo customerRepo) {
        this.customerRepo = customerRepo;
    }

    @EventListener
    public void sync(CustomerSyncEvent event) {
        Customer customer = Customer.builder()
                .id(event.customerId())
                .name(event.customerName())
                .isNewEntry(!customerRepo.existsById(event.customerId()))
                .build();
        customerRepo.save(customer);
    }

}
