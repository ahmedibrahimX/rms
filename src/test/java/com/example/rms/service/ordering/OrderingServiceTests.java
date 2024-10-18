package com.example.rms.service.ordering;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrderingServiceTests {
    @Test
    @DisplayName("Happy scenario. Order succeeds, No alerts.")
    public void happyScenario_shouldSucceed() throws Exception {
        throw new Exception("Not implemented");
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

    @Test
    @DisplayName("Product(s) with insufficient ingredient(s) branch stock. Should fail with descriptive exception")
    public void insufficientIngredients_shouldFailWithDescriptiveException() throws Exception {
        throw new Exception("Not implemented");
    }

    @Test
    @DisplayName("Sufficient ingredients for order but ingredient(s) branch stock will hit the threshold for the first time. Should succeed but alert the merchant about those first hits.")
    public void sufficientIngredientsButOneOrMoreIngredientStocksHitThresholdForFirstTime_shouldSucceed_alertMerchant() throws Exception {
        throw new Exception("Not implemented");
    }
}
