package com.example.rms.controller.ordering;

import com.example.rms.common.exception.handler.ErrorResponse;
import com.example.rms.controller.model.OrderRequest;
import com.example.rms.controller.model.OrderRequestItem;
import com.example.rms.controller.model.PlacedOrderItemResponse;
import com.example.rms.controller.model.PlacedOrderResponse;
import com.example.rms.infra.entity.OrderItem;
import com.example.rms.infra.repo.BranchRepo;
import com.example.rms.infra.repo.OrderItemRepo;
import com.example.rms.infra.repo.OrderRepo;
import com.example.rms.service.implementation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = {"local"})
public class OrderingControllerTest {
    @SpyBean
    OrderingPipelineService orderingPipelineService;
    @SpyBean
    OrderValidationService orderValidationService;
    @SpyBean
    RecipeService recipeService;
    @SpyBean
    ConsumptionCalculationService consumptionCalculationService;
    @SpyBean
    StockConsumptionService stockService;
    @SpyBean
    OrderPlacementService orderPlacementService;
    @SpyBean
    MerchantStockAlertMailingService merchantStockAlertMailingService;

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    BranchRepo branchRepo;
    @Autowired
    OrderRepo orderRepo;
    @Autowired
    OrderItemRepo orderItemRepo;


    @DisplayName("Integration test. Validate working through all layers.")
    @Test
    public void orderingWorksThroughAllLayers() throws Exception {
        var branchId = branchRepo.findAll().get(0).id();
        var orderRequest = new OrderRequest(List.of(new OrderRequestItem(1L, 4)));

        var result = mockMvc.perform(post("/api/v1/me/orders/{branchId}", branchId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(orderRequest))
                .header("Authorization", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI5YTA4YTYxNy04MTE2LTQwYWEtYWRhZC0wYWMwNzJkODUyODIiLCJuYW1lIjoiSm9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.48Zk9x0RvMNFfKcnhazz4ybSNi38gV6ro7F1AOVLNtI"))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), PlacedOrderResponse.class);
        assertEquals(1L, response.orderId());
        assertEquals(branchId, response.branchId());
        assertEquals("PLACED", response.status());
        var orderId = orderRepo.findAll().get(0).id();
        assertEquals(orderId, response.orderId());
        assertEquals(4, response.orderItems().size());
        Set<Long> responseProductIds = response.orderItems().stream().map(PlacedOrderItemResponse::productId).collect(Collectors.toSet());
        assertEquals(Set.of(1L), responseProductIds);
        Set<UUID> responseItemIds = response.orderItems().stream().map(PlacedOrderItemResponse::orderItemId).collect(Collectors.toSet());
        var orderItems = orderItemRepo.findAll();
        assertEquals(4, orderItems.size());
        assertTrue(responseItemIds.containsAll(orderItems.stream().map(OrderItem::id).collect(Collectors.toSet())));
        assertEquals(Set.of(1L), orderItems.stream().map(OrderItem::productId).collect(Collectors.toSet()));


        verify(orderingPipelineService, times(1)).placeOrder(any());
        verify(orderValidationService, times(1)).process(any());
        verify(recipeService, times(1)).process(any());
        verify(consumptionCalculationService, times(1)).process(any());
        verify(stockService, times(1)).process(any());
        verify(orderPlacementService, times(1)).process(any());
        verify(merchantStockAlertMailingService, times(1)).handle(any());
    }

    @DisplayName("Unauthorized access. Should return error response")
    @Test
    public void unauthorizedAccess_shouldReturnErrorResponse() throws Exception {
        var branchId = branchRepo.findAll().get(0).id();
        var orderRequest = new OrderRequest(List.of(new OrderRequestItem(1L, 4)));

        var result = mockMvc.perform(post("/api/v1/me/orders/{branchId}", branchId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isUnauthorized())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertEquals("Unauthorized", response.error());
    }

    @DisplayName("Missing product items in request. Should return error response")
    @Test
    public void missingProductItems_shouldReturnErrorResponse() throws Exception {
        var branchId = branchRepo.findAll().get(0).id();
        var orderRequest = new OrderRequest(new ArrayList<>());

        var result = mockMvc.perform(post("/api/v1/me/orders/{branchId}", branchId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertEquals("InvalidRequest", response.error());
    }

    @DisplayName("Product items is null in request. Should return error response")
    @Test
    public void productItemsIsNull_shouldReturnErrorResponse() throws Exception {
        var branchId = branchRepo.findAll().get(0).id();
        var orderRequest = new OrderRequest(null);

        var result = mockMvc.perform(post("/api/v1/me/orders/{branchId}", branchId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertEquals("InvalidRequest", response.error());
    }

    @DisplayName("Product id is invalid in request. Should return error response")
    @Test
    public void productIdIsInvalid_shouldReturnErrorResponse() throws Exception {
        var branchId = branchRepo.findAll().get(0).id();
        var orderRequest = new OrderRequest(List.of(new OrderRequestItem(-1L, 4)));

        var result = mockMvc.perform(post("/api/v1/me/orders/{branchId}", branchId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertEquals("InvalidRequest", response.error());
    }

    @DisplayName("Product id is null in request. Should return error response")
    @Test
    public void productIdIsNull_shouldReturnErrorResponse() throws Exception {
        var branchId = branchRepo.findAll().get(0).id();
        var orderRequest = new OrderRequest(List.of(new OrderRequestItem(null, 4)));

        var result = mockMvc.perform(post("/api/v1/me/orders/{branchId}", branchId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertEquals("InvalidRequest", response.error());
    }

    @DisplayName("Product quantity is invalid in request. Should return error response")
    @Test
    public void productQuantityIsInvalid_shouldReturnErrorResponse() throws Exception {
        var branchId = branchRepo.findAll().get(0).id();
        var orderRequest = new OrderRequest(List.of(new OrderRequestItem(1L, 0)));

        var result = mockMvc.perform(post("/api/v1/me/orders/{branchId}", branchId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertEquals("InvalidRequest", response.error());
    }

    @DisplayName("Product quantity is null in request. Should return error response")
    @Test
    public void productQuantityIsNull_shouldReturnErrorResponse() throws Exception {
        var branchId = branchRepo.findAll().get(0).id();
        var orderRequest = new OrderRequest(List.of(new OrderRequestItem(1L, null)));

        var result = mockMvc.perform(post("/api/v1/me/orders/{branchId}", branchId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertEquals("InvalidRequest", response.error());
    }
}
