package com.example.rms.service.ordering;

import com.example.rms.infra.entity.*;
import com.example.rms.infra.repo.*;
import com.example.rms.service.OrderingService;
import com.example.rms.service.StockService;
import com.example.rms.service.model.RequestedProductDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderingServiceTests {
    @Mock
    private ProductIngredientRepo productIngredientRepo;
    @Mock
    private IngredientStockRepo ingredientStockRepo;
    @Mock
    private StockService stockService;
    @Mock
    private OrderRepo orderRepo;
    @Mock
    private OrderItemRepo orderItemRepo;
    @Captor
    private ArgumentCaptor<Order> orderCaptor;
    @Captor
    private ArgumentCaptor<List<OrderItem>> orderItemCaptor;
    @Captor
    private ArgumentCaptor<UUID> branchIdCaptor;
    @Captor
    private ArgumentCaptor<Map<Long, Integer>> consumedIngredientsInGramsCaptor;

    @InjectMocks
    private OrderingService orderingService;

    private final UUID merchantId1 = UUID.randomUUID();
    private final Merchant merchant1 = new Merchant(merchantId1, "merchant1", "merchant@example.com");
    private final UUID customerId1 = UUID.randomUUID();
    private final UUID branchId1 = UUID.randomUUID();
    private final Branch branch1Merchant1 = new Branch(branchId1, merchantId1, "10", "26 July", "Zamalek", "Cairo", "Egypt");
    private final Long ingredientId1 = 1L;
    private final Ingredient ingredient1 = new Ingredient(ingredientId1, "ingredient1");
    private final Long ingredientId2 = 2L;
    private final Ingredient ingredient2 = new Ingredient(ingredientId2, "ingredient2");
    private final Long ingredientId3 = 3L;
    private final Ingredient ingredient3 = new Ingredient(ingredientId3, "ingredient2");
    private final Long productId1 = 1L;
    private final Product product1 = new Product(productId1, merchantId1, "product1");
    private final UUID product1Ingredient1Id = UUID.randomUUID();
    private final ProductIngredient product1Ingredient1 = new ProductIngredient(product1Ingredient1Id, productId1, ingredientId1, 100);
    private final UUID product1Ingredient2Id = UUID.randomUUID();
    private final ProductIngredient product1Ingredient2 = new ProductIngredient(product1Ingredient2Id, productId1, ingredientId2, 50);
    private final Long productId2 = 2L;
    private final Product product2 = new Product(productId2, merchantId1, "product2");
    private final UUID product2Ingredient1Id = UUID.randomUUID();
    private final ProductIngredient product2Ingredient1 = new ProductIngredient(product2Ingredient1Id, productId2, ingredientId1, 200);
    private final UUID product2Ingredient3Id = UUID.randomUUID();
    private final ProductIngredient product2Ingredient3 = new ProductIngredient(product2Ingredient3Id, productId2, ingredientId3, 100);
    private final Long productId3 = 3L;
    private final Product product3 = new Product(productId3, merchantId1, "product3");
    private final UUID product3Ingredient3Id = UUID.randomUUID();
    private final ProductIngredient product3Ingredient3 = new ProductIngredient(product3Ingredient3Id, productId3, ingredientId3, 50);

    @Test
    @DisplayName("Happy scenario. Order succeeds, No alerts.")
    public void happyScenario_shouldSucceed() throws Exception {
        when(productIngredientRepo.findAllByProductIdIn(any())).thenReturn(List.of(product1Ingredient1, product1Ingredient2, product2Ingredient1, product2Ingredient3, product3Ingredient3));
        when(orderRepo.save(any())).thenReturn(new Order(1L, branchId1, customerId1, "PLACED"));

        List<RequestedProductDetails> productRequests = List.of(new RequestedProductDetails(productId1, 2),
                new RequestedProductDetails(productId2, 1), new RequestedProductDetails(productId3, 1));
        orderingService.placeOrder(productRequests, customerId1, branchId1);

        verify(orderRepo, times(1)).save(orderCaptor.capture());
        assertEquals("PLACED", orderCaptor.getValue().status());
        assertEquals(branchId1, orderCaptor.getValue().branchId());
        assertEquals(customerId1, orderCaptor.getValue().customerId());
        Map<Long, Integer> expectedConsumedIngredientsInGrams = new HashMap<>();
        expectedConsumedIngredientsInGrams.put(1L, 400);
        expectedConsumedIngredientsInGrams.put(2L, 100);
        expectedConsumedIngredientsInGrams.put(3L, 150);
        verify(stockService, times(1)).updateStock(branchIdCaptor.capture(), consumedIngredientsInGramsCaptor.capture());
        assertEquals(branchId1, branchIdCaptor.getValue());
        assertEquals(3, consumedIngredientsInGramsCaptor.getValue().size());
        assertEquals(400, consumedIngredientsInGramsCaptor.getValue().get(1L));
        assertEquals(100, consumedIngredientsInGramsCaptor.getValue().get(2L));
        assertEquals(150, consumedIngredientsInGramsCaptor.getValue().get(3L));
        verify(orderItemRepo, times(1)).saveAll(orderItemCaptor.capture());
        assertEquals(4, orderItemCaptor.getValue().size());
        Map<Long, Long> orderItemsCountPerProduct = orderItemCaptor.getValue().stream().filter(oi -> Long.valueOf(1L).equals(oi.orderId()))
                .collect(Collectors.groupingBy(OrderItem::productId, Collectors.counting()));
        assertEquals(2, orderItemsCountPerProduct.get(1L));
        assertEquals(1, orderItemsCountPerProduct.get(2L));
        assertEquals(1, orderItemsCountPerProduct.get(3L));
    }

    @Test
    @DisplayName("Invalid product quantities. Should fail with descriptive exception")
    public void invalidQuantity_shouldFailWithDescriptiveException() throws Exception {
        throw new Exception("Not implemented");
    }

    @Test
    @DisplayName("Order product(s) not found under merchant. Should fail with descriptive exception")
    public void productNotFound_shouldFailWithDescriptiveException() throws Exception {
        throw new Exception("Not implemented");
    }
}
