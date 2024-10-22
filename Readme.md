# Coding Challenge

## Table of contents
1. [Brief description](#brief-description)
2. [Addressed challenges](#addressed-challenges)
3. [Highlights](#highlights)
4. [Technologies](#technologies-used-for-this-challenge)
5. [How to run](#how-to-run)
6. [Development approach](#development-approach)
7. [Testing techniques](#testing-techniques)
8. [Test Coverage](#test-coverage)
9. [CI pipeline](#ci)
10. [Database design](#database-design)
11. [API Documentation](#api-documentation)

## Brief description
> This is a system that is used by merchants and customers.
> The scope of this project is to implement the endpoint that accepts customer orders.
> - An order consists of one or more products, specifying the quantity ordered for each.
> - The system approves the order if the branch's stock contains enough ingredients for all the order products.
> - If the order is approved, the branch's stock of each used ingredient is decremented.
> - When an ingredient in branch's stock of is consumed and hits the 50% threshold, 
> an alerting email is to be sent to the merchant (only the first hit per ingredient should trigger the email)

## Addressed challenges
>The challenge's most basic idea is simple, a post API that accepts orders, however to design it to be production-grade here are some of the issues addressed:
> - Race condition: There's a possibility of race condition in case of having multiple orders that contain products that have common ingredients, the stock update done by one may be overwritten by the other, creating an inaccurate reporting to the merchant and causing revenue loss due not having enough ingredients without that being reflected on our system.
> - Failure recovery: In case of successfully applying stock consumption, but the order placement itself fails. Now you have reduced ingredient amounts in the database without actually using it in an order, creating another inaccurate reporting issue.

## Highlights
> - **Test driven development:** to write test suite that help me move from simple code towards better abstractions and patterns while having a strong test suite to support incremental refactoring. (This is the base for the next points)
> - **Pipeline design pattern and Single-Responsibility principle:** I split the logic into a pipeline of steps, each step has a single clear role in the pipeline
> - **Pipeline and Open-Closed principle:** the pipeline is build with generic interfaces, so if a teammate wants to add a step to the logic, all he has to do is to implement the interface and then using a single line of code he/she can add it to the pipeline. No need to do any change in any other code. So it's open for extension, closed for modification.
> - **Dependency inversion and dependency injection:** making the high-level entities rely on abstractions (interfaces) not implementations. Basically leveraging dependency injection to achieve decoupling.
> - **Optimistic locking and race conditions:** relying on DB transactions would downgrade the performance at scale, and using distributed locks is not suitable here. So optimistic locking is the most suitable choice in terms of business requirements as well as performance, however it will require a retry mechanism to have a full solution.
> - **Decorator design pattern and race condition (second part of the solution):** I implemented a decorator that takes a pipeline step and converts it into a retriable step, then used it to do automatic retrials (with exponential delay between trials) without any user action in case of hitting an optimistic lock. I also added a fall-back method in case of exhausting max retrials.
> - **Event-driven and decoupling components:** I used event driven for alerts as well as for reverting some failed operations while achieving good decoupling between components. It also gives me the power of non-blocking, asynchronous programming to improve performance.

## Technologies
- Java `17`
- Spring Boot Framework `3.3.4`
- Postgres
- Docker and docker compose
- Swagger for API documentation
- [Smtp4dev](https://github.com/rnwood/smtp4dev) (to see the smtp email feature working end-to-end)

## How to run
### Docker compose approach *(recommended)*
- Note: I am using docker compose `v2.25.0`
- This will run the entire setup, and you can start using the API
- From inside the project's folder run the following
```bash
sudo docker compose up
```

### Running each step manually (alternative to docker compose)
1. Download the latest package on my repo (created by my CI pipeline)
2. Run the following commands
   - Running a postgres container
     ```bash
     sudo docker run --name foodics -p 5432:5432 -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=rms -d postgres 
     ```
   - Running smtp4dev
     ```bash
     sudo docker run --rm -it -p 3000:80 -p 2525:25 rnwood/smtp4dev:v3
     ```
   - Running the jar
     ```bash
     java -Dspring.profiles.active=local -jar ./target/rms-0.0.1-SNAPSHOT.jar
     ```
### After running the project
- Here's a sample request you can run:
    ```curl
    curl --location 'http://localhost:8080/api/v1/me/orders/448f40a8-1ad1-43c4-84de-36672710bb80' \
    --header 'Authorization: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI5YTA4YTYxNy04MTE2LTQwYWEtYWRhZC0wYWMwNzJkODUyODIiLCJuYW1lIjoiSm9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.48Zk9x0RvMNFfKcnhazz4ybSNi38gV6ro7F1AOVLNtI' \
    --header 'Content-Type: application/json' \
    --data '{
        "products": [
                {
                    "productId": 1,
                    "quantity": 4
                }
            ]
    }'
    ```
- This request will consume seeded ingredients and make them exceed the threshold, so you can see the alert email sent to the merchant on smtp4dev at: http://localhost:3000/
- It will look like this screenshot
  ![mail demo screenshot](./screenshot/mail-demo.png)

## Development approach
> I decided to implement this challenge in test driven development (TDD)
1. Writing test cases for each of the requirements, these test cases should fail (no implementation yet).
2. Implement the simplest code possible to make the test cases pass.
3. Refactor the code and make sure no test cases are broken as a result.

## Testing techniques
> I combine unit tests with integration tests to validate that all components work together correctly on all levels at the same time I also validate that each unit has its logic correctly implemented.

# Test coverage

| Target          | Coverage |
|:----------------|:--------:|
| Class coverage  |   96%    |
| Method coverage |   88%    |
| Line coverage   |   93%    |
> I usually aim for 80-85% test coverage for normal unit testing. TDD is of a great use to increase coverage with effective test cases as seen here.

# CI
> I added a very simple CI pipeline using Github Actions to run the test suite (written in TDD), if all the tests pass, the workflow builds the project's artifact and publish it to Github packages.
 
> If the repo has more branches CI can be triggered upon merging a pull request, it can also contain more steps such as static code analysis (code style, security, ...) also deployment can be triggered upon successful merge to have a full CI/CD

> CI + TDD is a safety net; as no artifact is created unless the code passes all the test cases.

## Database design
> **Disclaimer:** I draw an initial ERD after reading the requirements to get an insight of how this can be modeled,
> but I will implement them as I go with each test case using TDD as mentioned above, so the actual implementation may simplify or improve on this
> design. Will update the ERD to reflect any changes.

> - Each product has ingredients with specified amounts
> - Each merchant has their own products
> - Each merchant has one or more branches

> PKs, FKs, and indexes are not shown in the diagram for simplicity.

```mermaid
erDiagram
    merchant ||--o{ branch: has
    merchant ||--o{ product: has
    branch ||--o{ ingredient_stock: has
    branch ||--o{ order: serves
    ingredient_stock }o--|| ingredient: of
    ingredient ||--o{ product_ingredient: is-used-in
    product ||--|{ product_ingredient: made-of
    customer ||--o{ order: places
    order ||--|{ order_item: made-of
    product ||--o{ order_item: is-used-in
    merchant {
        string name
        string email
    }
    branch {
        string building
        string street
        string region
        string city
        string country
    }
    ingredient {
        string name
    }
    ingredient_stock {
        numeric amount_in_kilos
        numeric max_capacity_in_kilos
    }
    product {
        string name
    }
    product_ingredient {
        numeric amount_in_grams
    }
    customer {
        string name
    }
    order {
        string status
        timestamp placedAt
    }
    order_item {
    }
```
## API documentation
- API documentation can be done using swagger. This can be done in 2 ways either
    - writing a yml file and uploading it to swaggerhub
    - or integrating swagger documentation in your codebase
- I chose the second approach for this challenge to keep the documentation close to the code and for ease of maintenance, the other approach has its usecases and I have found it useful in some situations as well, so it depends on the usecase.
- Once you run the project access the documentation at http://localhost:8080/swagger-ui/index.html#
- It will look like this screenshot
  ![api-docs](./screenshot/api-docs.png)
