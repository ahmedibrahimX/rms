package com.example.rms.service.ordering;

import com.example.rms.infra.entity.Branch;
import com.example.rms.infra.entity.Ingredient;
import com.example.rms.infra.entity.Merchant;
import com.example.rms.infra.repo.BranchRepo;
import com.example.rms.infra.repo.IngredientRepo;
import com.example.rms.service.IngredientStockAlertMailingService;
import com.example.rms.service.event.IngredientStockAlertEvent;
import com.example.rms.service.model.StockAmount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IngredientStockAlertMailingServiceTests {
    @Mock
    JavaMailSender emailSender;
    @Mock
    BranchRepo branchRepo;
    @Mock
    IngredientRepo ingredientRepo;
    @Captor
    ArgumentCaptor<SimpleMailMessage> mailCaptor;

    IngredientStockAlertMailingService mailingService;

    private final UUID merchantId1 = UUID.randomUUID();
    private final Merchant merchant = new Merchant(merchantId1, "merchant1", "merchant1@example.com");
    private final UUID branchId1 = UUID.randomUUID();
    private final Branch branch1Merchant1 = new Branch(branchId1, merchant, merchantId1, 10, "26-July", "Zamalek", "Cairo", "Egypt");

    @BeforeEach
    public void setup() {
        mailingService = new IngredientStockAlertMailingService(emailSender, branchRepo, ingredientRepo, "sender@example.com");
    }

    @Test
    @DisplayName("Sending Alert mail. Should succeed")
    public void sendingAlertMail_shouldSucceed() {
        when(branchRepo.findById(any())).thenReturn(Optional.of(branch1Merchant1));
        when(ingredientRepo.findById(any())).thenReturn(Optional.of(new Ingredient(1L, "beef")));

        mailingService.sendMail(new IngredientStockAlertEvent(this, branchId1, List.of(new StockAmount(1L, BigDecimal.valueOf(0.5)))));

        verify(emailSender, times(1)).send(mailCaptor.capture());
        assertEquals("sender@example.com", mailCaptor.getValue().getFrom());
        assertEquals("merchant1@example.com", Objects.requireNonNull(mailCaptor.getValue().getTo())[0]);
        assertEquals("Ingredient Stock Alert", mailCaptor.getValue().getSubject());
        assertEquals("Ingredients stock needs your attention, branch at 10 26-July, Zamalek, Cairo, Egypt for the following ingredients: \n    - beef, 0.5 kilos remaining\n", mailCaptor.getValue().getText());
    }

    @Test
    @DisplayName("Branch not found. Email should not be sent.")
    public void branchNotFound_shouldNotSendMail() {
        when(branchRepo.findById(any())).thenReturn(Optional.empty());

        mailingService.sendMail(new IngredientStockAlertEvent(this, branchId1, List.of(new StockAmount(1L, BigDecimal.valueOf(0.5)))));

        verifyNoInteractions(emailSender);
    }

    @Test
    @DisplayName("Ingredient not found. Email should not be sent.")
    public void ingredientNotFound_shouldNotSendMail() {
        when(branchRepo.findById(any())).thenReturn(Optional.of(branch1Merchant1));
        when(ingredientRepo.findById(any())).thenReturn(Optional.empty());

        mailingService.sendMail(new IngredientStockAlertEvent(this, branchId1, List.of(new StockAmount(1L, BigDecimal.valueOf(0.5)))));

        verifyNoInteractions(emailSender);
    }
}
