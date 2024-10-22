package com.example.rms.service.ordering;

import com.example.rms.infra.entity.IngredientStock;
import com.example.rms.infra.entity.Order;
import com.example.rms.infra.entity.OrderItem;
import com.example.rms.infra.entity.ProductIngredient;
import com.example.rms.infra.repo.OrderItemRepo;
import com.example.rms.infra.repo.OrderRepo;
import com.example.rms.service.OrderPlacementService;
import com.example.rms.service.event.OrderPlacementRevertedEvent;
import com.example.rms.service.exception.OrderPlacementFailedException;
import com.example.rms.service.model.abstraction.NewOrderWithConsumption;
import com.example.rms.service.model.abstraction.PersistedOrderItemDetails;
import com.example.rms.service.model.implementation.NewOrderPreparationDetails;
import com.example.rms.service.model.implementation.PlacedPersistedOrderDetails;
import com.example.rms.service.model.implementation.RequestedOrderItemDetails;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderPlacementServiceTests {
    @Mock
    private OrderRepo orderRepo;
    @Mock
    private OrderItemRepo orderItemRepo;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private OrderPlacementService orderPlacementService;
    @Captor
    private ArgumentCaptor<Order> orderCaptor;
    @Captor
    private ArgumentCaptor<List<OrderItem>> orderItemCaptor;
    @Captor
    private ArgumentCaptor<OrderPlacementRevertedEvent> eventCaptor;

    private final UUID branchId1 = UUID.randomUUID();
    private final Long ingredientId1 = 1L;
    private final Long ingredientId2 = 2L;
    private final Long ingredientId3 = 3L;
    private final Long productId1 = 1L;
    private final UUID product1Ingredient1Id = UUID.randomUUID();
    private final ProductIngredient product1Ingredient1 = new ProductIngredient(product1Ingredient1Id, productId1, ingredientId1, 100);
    private final UUID product1Ingredient2Id = UUID.randomUUID();
    private final ProductIngredient product1Ingredient2 = new ProductIngredient(product1Ingredient2Id, productId1, ingredientId2, 50);
    private final Long productId2 = 2L;
    private final UUID product2Ingredient1Id = UUID.randomUUID();
    private final ProductIngredient product2Ingredient1 = new ProductIngredient(product2Ingredient1Id, productId2, ingredientId1, 200);
    private final UUID product2Ingredient3Id = UUID.randomUUID();
    private final ProductIngredient product2Ingredient3 = new ProductIngredient(product2Ingredient3Id, productId2, ingredientId3, 100);
    private final Long productId3 = 3L;
    private final UUID product3Ingredient3Id = UUID.randomUUID();
    private final ProductIngredient product3Ingredient3 = new ProductIngredient(product3Ingredient3Id, productId3, ingredientId3, 50);
    @Captor
    private ArgumentCaptor<List<IngredientStock>> ingredientStockCaptor;

    @Test
    @DisplayName("Place order by creating the order with its items. Placing succeeds.")
    public void processOrder_shouldSucceed() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(orderRepo.save(any())).thenReturn(new Order(1L, branchId1, customerId, "PLACED"));
        UUID orderItemId1 = UUID.randomUUID();
        UUID orderItemId2 = UUID.randomUUID();
        UUID orderItemId3 = UUID.randomUUID();
        UUID orderItemId4 = UUID.randomUUID();
        when(orderItemRepo.saveAll(any())).thenReturn(List.of(
                new OrderItem(orderItemId1, productId1, 1L),
                new OrderItem(orderItemId2, productId1, 1L),
                new OrderItem(orderItemId3, productId2, 1L),
                new OrderItem(orderItemId4, productId3, 1L)
        ));

        List<RequestedOrderItemDetails> requestedItems = List.of(new RequestedOrderItemDetails(productId1, 2),
                new RequestedOrderItemDetails(productId2, 1), new RequestedOrderItemDetails(productId3, 1));
        NewOrderWithConsumption requestedOrder = new NewOrderPreparationDetails(branchId1, customerId, requestedItems);
        PlacedPersistedOrderDetails placedOrder = orderPlacementService.process(requestedOrder);

        assertEquals(1L, placedOrder.orderId());
        assertEquals(branchId1, placedOrder.branchId());
        assertEquals(customerId, placedOrder.customerId());
        assertEquals("PLACED", placedOrder.status());
        assertEquals(4, placedOrder.orderItems().size());
        Map<UUID, Long> orderItemProducts = placedOrder.orderItems().stream().collect(Collectors.toMap(PersistedOrderItemDetails::orderItemId, PersistedOrderItemDetails::productId));
        assertEquals(productId1, orderItemProducts.get(orderItemId1));
        assertEquals(productId1, orderItemProducts.get(orderItemId2));
        assertEquals(productId2, orderItemProducts.get(orderItemId3));
        assertEquals(productId3, orderItemProducts.get(orderItemId4));
        verify(orderRepo, times(1)).save(orderCaptor.capture());
        verify(orderRepo, times(1)).save(orderCaptor.capture());
        assertEquals("PLACED", orderCaptor.getValue().status());
        assertEquals(branchId1, orderCaptor.getValue().branchId());
        assertEquals(customerId, orderCaptor.getValue().customerId());
        verify(orderItemRepo, times(1)).saveAll(orderItemCaptor.capture());
        assertEquals(4, orderItemCaptor.getValue().size());
        Map<Long, Long> orderItemsCountPerProduct = orderItemCaptor.getValue().stream().filter(oi -> Long.valueOf(1L).equals(oi.orderId()))
                .collect(Collectors.groupingBy(OrderItem::productId, Collectors.counting()));
        assertEquals(2, orderItemsCountPerProduct.get(1L));
        assertEquals(1, orderItemsCountPerProduct.get(2L));
        assertEquals(1, orderItemsCountPerProduct.get(3L));
    }

    @Test
    @DisplayName("Order fails, should send an event and throw a descriptive exception.")
    public void orderFails_shouldSendAnEventAndThrowDescriptiveException() throws Exception {
        when(orderRepo.save(any())).thenThrow(new PersistenceException());

        List<RequestedOrderItemDetails> requestedItems = List.of(new RequestedOrderItemDetails(productId1, 2),
                new RequestedOrderItemDetails(productId2, 1), new RequestedOrderItemDetails(productId3, 1));
        NewOrderWithConsumption newOrderWithConsumption = new NewOrderPreparationDetails(branchId1, UUID.randomUUID(), requestedItems);
        assertThrows(OrderPlacementFailedException.class, () -> orderPlacementService.process(newOrderWithConsumption));

        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        assertEquals(newOrderWithConsumption, eventCaptor.getValue().newOrderWithConsumption());
    }
}
