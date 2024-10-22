package com.example.rms.service.implementation;

import com.example.rms.infra.entity.Branch;
import com.example.rms.infra.entity.Ingredient;
import com.example.rms.infra.repo.BranchRepo;
import com.example.rms.infra.repo.IngredientRepo;
import com.example.rms.service.abstraction.MerchantStockAlertEventHandler;
import com.example.rms.service.event.implementation.IngredientStockAlertEvent;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Optional;

@Slf4j
@Service
public class MerchantStockAlertMailingService implements MerchantStockAlertEventHandler {
    private final JavaMailSender emailSender;
    private final BranchRepo branchRepo;
    private final IngredientRepo ingredientRepo;
    private final String SUBJECT = "Ingredient Stock Alert";
    private final String EMAIL;
    private final String template = "Ingredients stock needs your attention, branch at {0} {1}, {2}, {3}, {4} for the following ingredients: \n";

    @Autowired
    public MerchantStockAlertMailingService(
            JavaMailSender emailSender,
            BranchRepo branchRepo,
            IngredientRepo ingredientRepo,
            @Value("${spring.mail.username}") String email
    ) {
        this.emailSender = emailSender;
        this.branchRepo = branchRepo;
        this.ingredientRepo = ingredientRepo;
        this.EMAIL = email;
    }

    @Async
    @EventListener
    @Transactional
    public void handle(IngredientStockAlertEvent event) {
        if (event.stockAmounts().isEmpty()) {
            log.error("Stock amounts are missing in the ingredient stock alert event.");
            return;
        }
        Optional<Branch> branchOptional = branchRepo.findById(event.branchId());
        if (branchOptional.isPresent()) {
            Branch branch = branchOptional.get();
            String merchantEmail = branch.merchant().email();
            StringBuilder body = new StringBuilder();
            body.append(MessageFormat.format(template, String.valueOf(branch.building()), branch.street(), branch.region(), branch.city(), branch.country()));
            for (var stock: event.stockAmounts()) {
                Optional<Ingredient> ingredient = ingredientRepo.findById(stock.ingredientId());
                if (ingredient.isEmpty()) {
                    log.error("Ingredient {} is not found", stock.ingredientId());
                    return;
                }
                body.append(MessageFormat.format("    - {0}, {1} kilos remaining\n", ingredient.get().name(), stock.amountInKilos()));
            }
            send(merchantEmail, SUBJECT, body.toString());
        } else {
            log.error("Branch {} is not found", event.branchId());
        }
    }

    private void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(EMAIL);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        emailSender.send(message);
        log.info("Email is sent to merchant");
    }
}
