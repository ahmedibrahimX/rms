package com.example.rms.seeder;

import com.example.rms.infra.entity.*;
import com.example.rms.infra.repo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@Profile("local")
public class DbSeeder implements CommandLineRunner {
    private final MerchantRepo merchantRepo;
    private final BranchRepo branchRepo;
    private final IngredientRepo ingredientRepo;
    private final ProductRepo productRepo;
    private final ProductIngredientRepo productIngredientRepo;
    private final IngredientStockRepo ingredientStockRepo;

    @Autowired
    public DbSeeder(MerchantRepo merchantRepo, BranchRepo branchRepo, IngredientRepo ingredientRepo, ProductRepo productRepo, ProductIngredientRepo productIngredientRepo, IngredientStockRepo ingredientStockRepo) {
        this.merchantRepo = merchantRepo;
        this.branchRepo = branchRepo;
        this.ingredientRepo = ingredientRepo;
        this.productRepo = productRepo;
        this.productIngredientRepo = productIngredientRepo;
        this.ingredientStockRepo = ingredientStockRepo;
    }

    @Override
    public void run(String... args) throws Exception {
    }

    @Bean
    CommandLineRunner commandLineRunner() {
        log.info("DB Seeder Running...");
        return args -> {
            Ingredient beef = ingredientRepo.save(new Ingredient("beef"));
            log.info("Created an ingredient: {}", beef);
            Ingredient cheese = ingredientRepo.save(new Ingredient("cheese"));
            log.info("Created an ingredient: {}", cheese);
            Ingredient onion = ingredientRepo.save(new Ingredient("onion"));
            log.info("Created an ingredient: {}", onion);
            Ingredient mushroom = ingredientRepo.save(new Ingredient("mushroom"));
            log.info("Created an ingredient: {}", mushroom);
            Merchant merchant = merchantRepo.save(new Merchant("willy's kitchen", "admin@willys.com"));
            log.info("Created a merchant: {}", merchant);
            Product originalCheeseBurger = productRepo.save(new Product(merchant.id(), "original cheese burger"));
            log.info("Created a product: {}", originalCheeseBurger);
            ProductIngredient originalCheeseIngredient1 = productIngredientRepo.save(new ProductIngredient(originalCheeseBurger.id(), beef.id(), 150));
            log.info("Created a product ingredient: {}", originalCheeseIngredient1);
            ProductIngredient originalCheeseIngredient2 = productIngredientRepo.save(new ProductIngredient(originalCheeseBurger.id(), cheese.id(), 30));
            log.info("Created a product ingredient: {}", originalCheeseIngredient2);
            ProductIngredient originalCheeseIngredient3 = productIngredientRepo.save(new ProductIngredient(originalCheeseBurger.id(), onion.id(), 20));
            log.info("Created a product ingredient: {}", originalCheeseIngredient3);
            Product brooklynShroomsBurger = productRepo.save(new Product(merchant.id(), "brooklyn shrooms"));
            log.info("Created a product: {}", brooklynShroomsBurger);
            ProductIngredient brooklynShroomsIngredient1 = productIngredientRepo.save(new ProductIngredient(brooklynShroomsBurger.id(), beef.id(), 200));
            log.info("Created a product ingredient: {}", brooklynShroomsIngredient1);
            ProductIngredient brooklynShroomsIngredient2 = productIngredientRepo.save(new ProductIngredient(brooklynShroomsBurger.id(), cheese.id(), 50));
            log.info("Created a product ingredient: {}", brooklynShroomsIngredient2);
            ProductIngredient brooklynShroomsIngredient3 = productIngredientRepo.save(new ProductIngredient(brooklynShroomsBurger.id(), mushroom.id(), 70));
            log.info("Created a product ingredient: {}", brooklynShroomsIngredient3);
            Branch dokkiBranch = branchRepo.save(new Branch(UUID.fromString("448f40a8-1ad1-43c4-84de-36672710bb80"), merchant.id(), 5, "Al Narges", "Dokki", "Giza", "Egypt"));
            log.info("Created a branch: {}", dokkiBranch);
            IngredientStock dokkiBeefStock = ingredientStockRepo.save(new IngredientStock(dokkiBranch.id(), beef.id(), BigDecimal.valueOf(1), BigDecimal.valueOf(1.5)));
            log.info("Created a stock: {}", dokkiBeefStock);
            IngredientStock dokkiCheeseStock = ingredientStockRepo.save(new IngredientStock(dokkiBranch.id(), cheese.id(), BigDecimal.valueOf(1.6), BigDecimal.valueOf(3)));
            log.info("Created a stock: {}", dokkiCheeseStock);
            IngredientStock dokkiOnionStock = ingredientStockRepo.save(new IngredientStock(dokkiBranch.id(), onion.id(), BigDecimal.valueOf(10), BigDecimal.valueOf(30)));
            log.info("Created a stock: {}", dokkiOnionStock);
            IngredientStock dokkiMushroomStock = ingredientStockRepo.save(new IngredientStock(dokkiBranch.id(), mushroom.id(), BigDecimal.valueOf(30), BigDecimal.valueOf(30)));
            log.info("Created a stock: {}", dokkiMushroomStock);
            log.info("DB Seeder Done...");
        };
    }
}
