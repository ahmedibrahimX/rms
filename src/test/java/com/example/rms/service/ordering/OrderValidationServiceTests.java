package com.example.rms.service.ordering;

import com.example.rms.infra.entity.Branch;
import com.example.rms.infra.repo.BranchRepo;
import com.example.rms.infra.repo.ProductIngredientRepo;
import com.example.rms.infra.repo.ProductRepo;
import com.example.rms.service.implementation.OrderValidationService;
import com.example.rms.service.exception.OrderValidationException;
import com.example.rms.service.model.implementation.NewOrderPreparationDetails;
import com.example.rms.service.model.implementation.RequestedOrderItemDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderValidationServiceTests {
    @Mock
    private ProductRepo productRepo;
    @Mock
    private BranchRepo branchRepo;
    @Mock
    private ProductIngredientRepo productIngredientRepo;
    @Captor
    private ArgumentCaptor<UUID> branchIdCaptor;
    @Captor
    private ArgumentCaptor<UUID> merchantIdCaptor;
    @Captor
    private ArgumentCaptor<Set<Long>> productIdCaptor;

    private OrderValidationService orderValidationService;

    private final UUID merchantId1 = UUID.randomUUID();
    private final UUID branchId1 = UUID.randomUUID();
    private final Branch branch1Merchant1 = new Branch(branchId1, merchantId1, 10, "26 July", "Zamalek", "Cairo", "Egypt");
    private final Long productId1 = 1L;
    private final Long productId2 = 2L;
    private final Long productId3 = 3L;

    @BeforeEach
    public void setup() {
        orderValidationService = new OrderValidationService(productRepo, branchRepo, productIngredientRepo, 10L, 5L);
    }

    @Test
    @DisplayName("Happy scenario. Succeeds.")
    public void happyScenario_shouldSucceed() {
        when(branchRepo.findById(any())).thenReturn(Optional.of(branch1Merchant1));
        when(productRepo.countByMerchantIdAndIdIn(any(), any())).thenReturn(3L);
        when(productIngredientRepo.countByProductId(1L)).thenReturn(2L);
        when(productIngredientRepo.countByProductId(2L)).thenReturn(2L);
        when(productIngredientRepo.countByProductId(3L)).thenReturn(1L);

        List<RequestedOrderItemDetails> requestedItems = List.of(new RequestedOrderItemDetails(productId1, 2),
                new RequestedOrderItemDetails(productId2, 1), new RequestedOrderItemDetails(productId3, 1));
        NewOrderPreparationDetails order = new NewOrderPreparationDetails(branchId1, UUID.randomUUID(), requestedItems);
        orderValidationService.process(order);

        verify(branchRepo, times(1)).findById(branchIdCaptor.capture());
        assertEquals(branchId1, branchIdCaptor.getValue());
        verify(productRepo, times(1)).countByMerchantIdAndIdIn(merchantIdCaptor.capture(), productIdCaptor.capture());
        assertEquals(merchantId1, merchantIdCaptor.getValue());
        assertTrue(Set.of(productId1, productId2, productId3).containsAll(productIdCaptor.getValue()));
    }

    @Test
    @DisplayName("No product details provided. Should fail with descriptive exception.")
    public void noProductDetailsProvided_returnsInvalid() {
        NewOrderPreparationDetails order = new NewOrderPreparationDetails(branchId1, UUID.randomUUID(), new ArrayList<>());
        assertThrows(OrderValidationException.class, () -> orderValidationService.process(order));

        verifyNoInteractions(branchRepo);
        verifyNoInteractions(productRepo);
    }

    @Test
    @DisplayName("Branch not found. Should fail with descriptive exception.")
    public void branchNotFound_returnsInvalid() {
        when(branchRepo.findById(any())).thenReturn(Optional.empty());
        List<RequestedOrderItemDetails> requestedItems = List.of(new RequestedOrderItemDetails(productId1, 2),
                new RequestedOrderItemDetails(productId2, 1), new RequestedOrderItemDetails(productId3, 1));
        NewOrderPreparationDetails order = new NewOrderPreparationDetails(branchId1, UUID.randomUUID(), requestedItems);
        assertThrows(OrderValidationException.class, () -> orderValidationService.process(order));
    }

    @Test
    @DisplayName("Product not found per merchant. Should fail with descriptive exception.")
    public void productNotFoundPerMerchant_returnsInvalid() {
        when(branchRepo.findById(any())).thenReturn(Optional.of(branch1Merchant1));
        when(productRepo.countByMerchantIdAndIdIn(any(), any())).thenReturn(2L);
        List<RequestedOrderItemDetails> requestedItems = List.of(new RequestedOrderItemDetails(productId1, 2),
                new RequestedOrderItemDetails(productId2, 1), new RequestedOrderItemDetails(productId3, 1));
        NewOrderPreparationDetails order = new NewOrderPreparationDetails(branchId1, UUID.randomUUID(), requestedItems);
        assertThrows(OrderValidationException.class, () -> orderValidationService.process(order));
    }

    @Test
    @DisplayName("A product ingredient missing. Should fail with descriptive exception")
    public void productIngredientMissing_shouldFailWithDescriptiveException() {
        when(branchRepo.findById(any())).thenReturn(Optional.of(branch1Merchant1));
        when(productRepo.countByMerchantIdAndIdIn(any(), any())).thenReturn(3L);
        when(productIngredientRepo.countByProductId(1L)).thenReturn(0L);
        List<RequestedOrderItemDetails> requestedItems = List.of(new RequestedOrderItemDetails(productId1, 2),
                new RequestedOrderItemDetails(productId2, 1), new RequestedOrderItemDetails(productId3, 1));
        NewOrderPreparationDetails order = new NewOrderPreparationDetails(branchId1, UUID.randomUUID(), requestedItems);
        assertThrows(OrderValidationException.class, () -> orderValidationService.process(order));
    }

    @Test
    @DisplayName("Product quantity limit exceeded. Should fail with descriptive exception.")
    public void productQuantityLimitExceeded_returnsInvalid() {
        when(branchRepo.findById(any())).thenReturn(Optional.of(branch1Merchant1));
        when(productRepo.countByMerchantIdAndIdIn(any(), any())).thenReturn(3L);
        when(productIngredientRepo.countByProductId(1L)).thenReturn(2L);
        List<RequestedOrderItemDetails> requestedItems = List.of(new RequestedOrderItemDetails(productId1, 6),
                new RequestedOrderItemDetails(productId2, 1), new RequestedOrderItemDetails(productId3, 1));
        NewOrderPreparationDetails order = new NewOrderPreparationDetails(branchId1, UUID.randomUUID(), requestedItems);
        assertThrows(OrderValidationException.class, () -> orderValidationService.process(order));
    }

    @Test
    @DisplayName("Total quantity limit exceeded. Should fail with descriptive exception.")
    public void totalQuantityLimitExceeded_returnsInvalid() {
        when(branchRepo.findById(any())).thenReturn(Optional.of(branch1Merchant1));
        when(productRepo.countByMerchantIdAndIdIn(any(), any())).thenReturn(3L);
        when(productIngredientRepo.countByProductId(1L)).thenReturn(2L);
        when(productIngredientRepo.countByProductId(2L)).thenReturn(2L);
        when(productIngredientRepo.countByProductId(3L)).thenReturn(1L);
        List<RequestedOrderItemDetails> requestedItems = List.of(new RequestedOrderItemDetails(productId1, 5),
                new RequestedOrderItemDetails(productId2, 5), new RequestedOrderItemDetails(productId3, 1));
        NewOrderPreparationDetails order = new NewOrderPreparationDetails(branchId1, UUID.randomUUID(), requestedItems);
        assertThrows(OrderValidationException.class, () -> orderValidationService.process(order));
    }
}
