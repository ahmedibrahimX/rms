package com.example.rms.service.ordering;

import com.example.rms.infra.entity.IngredientStock;
import com.example.rms.infra.entity.Order;
import com.example.rms.infra.entity.OrderItem;
import com.example.rms.infra.entity.ProductIngredient;
import com.example.rms.infra.repo.OrderItemRepo;
import com.example.rms.infra.repo.OrderRepo;
import com.example.rms.service.OrderPreparationService;
import com.example.rms.service.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderPreparationServiceTests {
    @Mock
    private OrderRepo orderRepo;
    @Mock
    private OrderItemRepo orderItemRepo;
    @InjectMocks
    private OrderPreparationService orderPreparationService;
    @Captor
    private ArgumentCaptor<Order> orderCaptor;
    @Captor
    private ArgumentCaptor<List<OrderItem>> orderItemCaptor;

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
    @DisplayName("Get total ingredient amounts in grams. Getting amounts succeeds.")
    public void getTotalIngredientAmountsInGrams_shouldSucceed() throws Exception {
        ProductRecipe productRecipe1 = new ProductRecipe(productId1, List.of(new IngredientAmount(ingredientId1, product1Ingredient1.amountInGrams()), new IngredientAmount(ingredientId2, product1Ingredient2.amountInGrams())));
        ProductRecipe productRecipe2 = new ProductRecipe(productId2, List.of(new IngredientAmount(ingredientId1, product2Ingredient1.amountInGrams()), new IngredientAmount(ingredientId3, product2Ingredient3.amountInGrams())));
        ProductRecipe productRecipe3 = new ProductRecipe(productId3, List.of(new IngredientAmount(ingredientId3, product3Ingredient3.amountInGrams())));
        List<ProductRecipe> recipes = List.of(productRecipe1, productRecipe2, productRecipe3);

        List<RequestedProductDetails> productRequests = List.of(new RequestedProductDetails(productId1, 2),
                new RequestedProductDetails(productId2, 1), new RequestedProductDetails(productId3, 1));
        List<IngredientAmount> amounts = orderPreparationService.getTotalAmountsInGrams(recipes, productRequests);

        assertEquals(3, amounts.size());
        Map<Long, Integer> amountsMap = amounts.stream().collect(Collectors.toMap(IngredientAmount::ingredientId, IngredientAmount::amountInGrams));
        assertEquals(400, amountsMap.get(ingredientId1));
        assertEquals(100, amountsMap.get(ingredientId2));
        assertEquals(150, amountsMap.get(ingredientId3));
    }

    @Test
    @DisplayName("Place order by creating the order with its items. Placing succeeds.")
    public void placeOrder_shouldSucceed() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(orderRepo.save(any())).thenReturn(new Order(1L, branchId1, customerId, "PLACED"));

        List<RequestedProductDetails> productRequests = List.of(new RequestedProductDetails(productId1, 2),
                new RequestedProductDetails(productId2, 1), new RequestedProductDetails(productId3, 1));
        OrderDetails order = orderPreparationService.place(productRequests, customerId, branchId1);

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
}
