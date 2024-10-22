package com.example.rms.service.event.implementation;

import com.example.rms.infra.entity.Customer;
import com.example.rms.infra.repo.CustomerRepo;
import com.example.rms.service.abstraction.CustomerSyncEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class CustomerService implements CustomerSyncEventHandler {
    private CustomerRepo customerRepo;

    @Autowired
    public CustomerService(CustomerRepo customerRepo) {
        this.customerRepo = customerRepo;
    }

    @EventListener
    public void handle(CustomerSyncEvent event) {
        Customer customer = Customer.builder()
                .id(event.customerId())
                .name(event.customerName())
                .isNewEntry(!customerRepo.existsById(event.customerId()))
                .build();
        customerRepo.save(customer);
    }

}
