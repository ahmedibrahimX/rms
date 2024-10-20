package com.example.rms.service.ordering;

import com.example.rms.infra.entity.Branch;
import com.example.rms.infra.repo.BranchRepo;
import com.example.rms.infra.repo.ProductRepo;
import com.example.rms.service.OrderValidationService;
import com.example.rms.service.model.RequestedProductDetails;
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
public class OrderValidtionServiceTests {
    @Mock
    private ProductRepo productRepo;
    @Mock
    private BranchRepo branchRepo;
    @Captor
    private ArgumentCaptor<UUID> branchIdCaptor;
    @Captor
    private ArgumentCaptor<UUID> merchantIdCaptor;
    @Captor
    private ArgumentCaptor<Set<Long>> productIdCaptor;

    private OrderValidationService orderValidationService;

    private final UUID merchantId1 = UUID.randomUUID();
    private final UUID branchId1 = UUID.randomUUID();
    private final Branch branch1Merchant1 = new Branch(branchId1, merchantId1, "10", "26 July", "Zamalek", "Cairo", "Egypt");
    private final Long productId1 = 1L;
    private final Long productId2 = 2L;
    private final Long productId3 = 3L;

    @BeforeEach
    public void setup() {
        orderValidationService = new OrderValidationService(productRepo, branchRepo, 10L, 5L);
    }

    @Test
    @DisplayName("Happy scenario. Succeeds.")
    public void happyScenario_shouldSucceed() throws Exception {
        when(branchRepo.findById(any())).thenReturn(Optional.of(branch1Merchant1));
        when(productRepo.countByMerchantIdAndIdIn(any(), any())).thenReturn(3L);

        List<RequestedProductDetails> productRequests = List.of(new RequestedProductDetails(productId1, 2),
                new RequestedProductDetails(productId2, 1), new RequestedProductDetails(productId3, 1));
        boolean isValid = orderValidationService.validate(productRequests, branchId1);

        verify(branchRepo, times(1)).findById(branchIdCaptor.capture());
        assertEquals(branchId1, branchIdCaptor.getValue());
        verify(productRepo, times(1)).countByMerchantIdAndIdIn(merchantIdCaptor.capture(), productIdCaptor.capture());
        assertEquals(merchantId1, merchantIdCaptor.getValue());
        assertTrue(Set.of(productId1, productId2, productId3).containsAll(productIdCaptor.getValue()));
        assertTrue(isValid);
    }

    @Test
    @DisplayName("No product details provided. Returns invalid.")
    public void noProductDetailsProvided_returnsInvalid() throws Exception {
        List<RequestedProductDetails> productRequests = new ArrayList<>();
        boolean isValid = orderValidationService.validate(productRequests, branchId1);

        assertFalse(isValid);
        verifyNoInteractions(branchRepo);
        verifyNoInteractions(productRepo);
    }

    @Test
    @DisplayName("Branch not found. Returns invalid.")
    public void branchNotFound_returnsInvalid() throws Exception {
        when(branchRepo.findById(any())).thenReturn(Optional.empty());
        List<RequestedProductDetails> productRequests = List.of(new RequestedProductDetails(productId1, 2),
                new RequestedProductDetails(productId2, 1), new RequestedProductDetails(productId3, 1));
        boolean isValid = orderValidationService.validate(productRequests, branchId1);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Product not found per merchant. Returns invalid.")
    public void productNotFoundPerMerchant_returnsInvalid() throws Exception {
        when(branchRepo.findById(any())).thenReturn(Optional.of(branch1Merchant1));
        when(productRepo.countByMerchantIdAndIdIn(any(), any())).thenReturn(2L);
        List<RequestedProductDetails> productRequests = List.of(new RequestedProductDetails(productId1, 2),
                new RequestedProductDetails(productId2, 1), new RequestedProductDetails(productId3, 1));
        boolean isValid = orderValidationService.validate(productRequests, branchId1);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Product quantity limit exceeded. Returns invalid.")
    public void productQuantityLimitExceeded_returnsInvalid() throws Exception {
        when(branchRepo.findById(any())).thenReturn(Optional.of(branch1Merchant1));
        when(productRepo.countByMerchantIdAndIdIn(any(), any())).thenReturn(3L);
        List<RequestedProductDetails> productRequests = List.of(new RequestedProductDetails(productId1, 6),
                new RequestedProductDetails(productId2, 1), new RequestedProductDetails(productId3, 1));
        boolean isValid = orderValidationService.validate(productRequests, branchId1);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Total quantity limit exceeded. Returns invalid.")
    public void totalQuantityLimitExceeded_returnsInvalid() throws Exception {
        when(branchRepo.findById(any())).thenReturn(Optional.of(branch1Merchant1));
        when(productRepo.countByMerchantIdAndIdIn(any(), any())).thenReturn(3L);
        List<RequestedProductDetails> productRequests = List.of(new RequestedProductDetails(productId1, 5),
                new RequestedProductDetails(productId2, 5), new RequestedProductDetails(productId3, 1));
        boolean isValid = orderValidationService.validate(productRequests, branchId1);

        assertFalse(isValid);
    }
}
