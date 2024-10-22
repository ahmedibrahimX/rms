# Coding Challenge

## Table of contents
1. [Brief description](#brief-description)
2. [Addressed challenges](#addressed-challenges)
3. [Highlights](#highlights)
4. [Technologies](#technologies)
5. [How to run](#how-to-run)
6. [Development approach](#development-approach)
7. [Testing techniques](#testing-techniques)
8. [Test Coverage](#test-coverage)
9. [CI pipeline](#ci)
10. [Database design](#database-design)
11. [Folder structure](#folder-structure)
12. [API Documentation](#api-documentation)
13. [Enhancement points](#enhancement-points)

## Brief description
> This is a restaurant system, used by merchants and customers. The scope of this challenge is:
> - Customer Ordering API: Accepts customer requests consisting of multiple products and their quantities.
> - Stock Management: 
>   - Validates order fulfillment based on branch stock levels.
>   - Applies ingredient consumption.
>   - Stock level tracking and monitoring for critical threshold.
> - Order Placement: Places order on the system after validation.
> - Alerting System: Tracks ingredient stock levels and sends alerts (email notifications to merchant) when they reach a critical threshold (50%).
>  
> By combining these elements, the system ensures efficient order processing and proactive inventory management.

:point_up: [Back to top](#table-of-contents)

## Addressed challenges
>The challenge's most basic idea is simple, a post API that accepts orders, however to design it to be production-grade here are some of the issues addressed:
> - Race condition: There's a possibility of race condition in case of having multiple orders that contain products that have common ingredients, the stock update done by one may be overwritten by the other, creating an inaccurate reporting to the merchant and causing bad customer experience and revenue loss; rejecting customer orders manually due to not having enough ingredients in the branch stock without that being reflected on our system.
> - Failure recovery: In case of successfully applying stock consumption, but the order placement itself fails. Now you have reduced ingredient amounts in the database without actually using it in an order, in other words consuming the ingredients on the system but not in reality, this will cause another inaccurate reporting issue for the merchant, and in the worst case an ingredient stock level may be decremented in the database until the system cannot accept orders that require this ingredient also causing revenue loss.

:point_up: [Back to top](#table-of-contents)

## Highlights
> - **Test driven development:** to write test cases that help me move from simple code towards better abstractions and patterns while having a strong test suite to support incremental refactoring. (This is the base for the next points)
> - **Pipeline design pattern and Single-Responsibility principle:** I split the logic into a pipeline of steps, each step has a single clear role in the pipeline
> - **Pipeline and Open-Closed principle:** the pipeline is build with generic interfaces, so if a teammate wants to add a step to the logic, all he has to do is to implement the interface and then using a single line of code he/she can add it to the pipeline. No need to do any change in any other code. So it's open for extension, closed for modification.
> - **Dependency inversion and dependency injection:** making the high-level entities rely on abstractions (interfaces) not implementations. Basically leveraging dependency injection to achieve decoupling.
> - **Optimistic locking and race conditions:** relying on DB transactions would downgrade the performance at scale, and using distributed locks is not suitable here. So optimistic locking is the most suitable choice in terms of business requirements as well as performance, however it will require a retry mechanism to have a full solution.
> - **Decorator design pattern and race condition (second part of the solution):** I implemented a decorator that takes a pipeline step and converts it into a retriable step, then used it to do automatic retrials (with exponential delay between trials) without any user action in case of hitting an optimistic lock. I also added a fall-back method in case of exhausting max retrials.
> - **Event-driven and decoupling components:** I used event driven programming for alerts as well as for reverting some failed operations while achieving good decoupling between components. It also gives me the power of non-blocking, asynchronous programming to improve performance.

:point_up: [Back to top](#table-of-contents)

## Technologies
- Java `17`
- Spring Boot Framework `3.3.4`
- Postgres
- Docker and docker compose
- Swagger for API documentation
- [Smtp4dev](https://github.com/rnwood/smtp4dev) (to see the smtp email feature working end-to-end)

:point_up: [Back to top](#table-of-contents)

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
- You can view the seeded DB values [here](#database-seeding) :point_down:
- It will look like this screenshot
  ![mail demo screenshot](./screenshot/mail-demo.png)
:point_up: [Back to top](#table-of-contents)

## Development approach
> I decided to implement this challenge in test driven development (TDD)
1. Writing test cases for each of the requirements, these test cases should fail (no implementation yet).
2. Implement the simplest code possible to make the test cases pass.
3. Refactor the code and make sure no test cases are broken as a result.

:point_up: [Back to top](#table-of-contents)

## Testing techniques
> I combine unit tests with integration tests to validate that all components work together correctly on all levels at the same time I also validate that each unit has its logic correctly implemented.

:point_up: [Back to top](#table-of-contents)

## Test coverage

| Target          | Coverage |
|:----------------|:--------:|
| Class coverage  |   96%    |
| Method coverage |   88%    |
| Line coverage   |   93%    |
> I usually aim for 80-85% test coverage for normal unit testing. TDD is of a great use to increase coverage with effective test cases as seen here.

:point_up: [Back to top](#table-of-contents)

## CI
> I added a very simple CI pipeline using Github Actions to run the test suite (written in TDD), if all the tests pass, the workflow builds the project's artifact and publish it to Github packages.
 
> If the repo has more branches CI can be triggered upon merging a pull request, it can also contain more steps such as static code analysis (code style, security, ...) also deployment can be triggered upon successful merge to have a full CI/CD

> CI + TDD is a safety net; as no artifact is created unless the code passes all the test cases.

:point_up: [Back to top](#table-of-contents)

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
### Database seeding
:point_up: [Back to how to run section](#after-running-the-project)
#### Ingredient
| id  |   name   |
|:---:|:--------:|
|  1  |   beef   |
|  2  |  cheese  |
|  3  |  onion   |
|  4  | mushroom |

#### Merchant

|       id        |      name       |      email       |
|:---------------:|:---------------:|:----------------:|
| auto-generated  | willy's kitchen | admin@willys.com |

#### Branch

|                  id                  |        merchantId         | building |  street   | region | city | country |
|:------------------------------------:|:-------------------------:|:--------:|:---------:|:------:|:----:|:-------:|
| 448f40a8-1ad1-43c4-84de-36672710bb80 | Id of the seeded merchant |    5     | Al Narges | Dokki  | Giza |  Egypt  |
> Branch id is set by the seeder (not auto-generated) to ease the testing for the challenge judge, as the branch id is needed in the API, otherwise he would need to get it from the logs.

#### Product
| id  |        merchantId         |          name          |
|:---:|:-------------------------:|:----------------------:|
|  1  | Id of the seeded merchant | original cheese burger |
|  2  | Id of the seeded merchant |    brooklyn shrooms    |

#### Product Ingredient
|       id        | productId | ingredientId | amountInGrams |
|:---------------:|:---------:|:------------:|:-------------:|
| auto-generated  |     1     |      1       |      150      |
| auto-generated  |     1     |      2       |      30       |
| auto-generated  |     1     |      3       |      20       |
| auto-generated  |     2     |      1       |      200      |
| auto-generated  |     2     |      2       |      50       |
| auto-generated  |     2     |      4       |      70       |

#### Ingredient Stock

|       id        | ingredientId |               branchId               | amountInKilos | maxCapacityInKilos |
|:---------------:|:------------:|:------------------------------------:|:-------------:|:------------------:|
| auto-generated  |      1       | 448f40a8-1ad1-43c4-84de-36672710bb80 |       1       |        1.5         |
| auto-generated  |      2       | 448f40a8-1ad1-43c4-84de-36672710bb80 |      1.6      |         3          |
| auto-generated  |      3       | 448f40a8-1ad1-43c4-84de-36672710bb80 |      10       |         30         |
| auto-generated  |      4       | 448f40a8-1ad1-43c4-84de-36672710bb80 |      30       |         30         |

- :point_up: [Back to how to run section](#after-running-the-project)
- :point_up: [Back to top](#table-of-contents)

## Folder structure
> This section shows you the project's folder structure + some notes that might help the challenge judge with navigating the code
  
```
.
├── docker-compose.yml
├── Dockerfile
├── pom.xml (dependencies, plugins, maven configs, ...)
├── Readme.md
├── /src
│   ├── /main
│   │   ├── /java
│   │   │   └── /com
│   │   │       └── /example
│   │   │           └── /rms
│   │   │               ├── /common
│   │   │               │   ├── /auth
│   │   │               │   │   ├── RequireUser.java (custom method annotation for user auth)
│   │   │               │   │   └── UserPermission.java (auth logic, runs for @RequireUser annotated methods)
│   │   │               │   ├── /config
│   │   │               │   │   └── AsyncConfiguration.java (configuring aysnc behavior for the app)
│   │   │               │   ├── /docs
│   │   │               │   │   └── SwaggerConfig.java (integrating swagger in the project)
│   │   │               │   ├── /exception
│   │   │               │   │   └── handler
│   │   │               │   │       └── GlobalAdvice.java (global exception handling for the project)
│   │   │               │   └── /util (contains utility/helper classes)
│   │   │               ├── /controller
│   │   │               │   ├── /mapper
│   │   │               │   │   └── OrderDetailsMapper.java (simple mapping: request --> service layer input & service layer output --> response, to decouple the service layer)
│   │   │               │   ├── /model (contains request and response models)
│   │   │               │   ├── OrderingController.java
│   │   │               │   └── /validation
│   │   │               │       └── UUIDPattern.java (custom annotation to validate uuid format)
│   │   │               ├── /infra (data layer)
│   │   │               │   ├── /entity (infrastructure entities)
│   │   │               │   └── /repo (repo interfaces: interfaces only; implementation is provided by ORM)
│   │   │               ├── RmsApplication.java (Spring boot's entry point)
│   │   │               ├── /seeder
│   │   │               │   └── DbSeeder.java
│   │   │               └── /service
│   │   │                   ├── /abstraction (service layer interfaces)
│   │   │                   ├── /implementation (service layer implementations)
│   │   │                   ├── /event (custom events)
│   │   │                   ├── /exception (custom exceptions)
│   │   │                   ├── /model (service layer models)
│   │   │                   └── /pattern (design patterns)
│   │   │                       ├── /decorator
│   │   │                       │   └── RetriableStepDecorator.java
│   │   │                       └── /pipeline
│   │   │                           ├── Pipeline.java
│   │   │                           └── Step.java
│   │   └── /resources
│   │       ├── application.yml (general app config)
│   │       ├── application-local.yml (app config for local environment) 
│   └── /test
│       └── /java
│           └── /com
│               └── /example
│                   └── /rms
│                       ├── /controller (controller level tests)
│                       └── /service (service level tests)
```
:point_up: [Back to top](#table-of-contents)

## API documentation
- API documentation can be done using swagger. This can be done in 2 ways either
    - writing a yml file and uploading it to swaggerhub
    - or integrating swagger documentation in your codebase
- I chose the second approach for this challenge to keep the documentation close to the code and for ease of maintenance, the other approach has its usecases and I have found it useful in some situations as well, so it depends on the usecase.
- Once you run the project access the documentation at http://localhost:8080/swagger-ui/index.html#
- It will look like this screenshot
  ![api-docs](./screenshot/api-docs.png)
:point_up: [Back to top](#table-of-contents)

## Enhancement points
> If this challenge was a true production task, I would also want to add the following:
> - Structured logging in a searchable format (e.g. json)
> - Custom metrics for monitoring the app + a time series database to analyze the metrics over time and send alerts (e.g. Prometheus) + a visualization tool (e.g. Grafana). 
>   - I wrote an article about that before on medium check it here: [From Chaos to Clarity](https://blog.devops.dev/from-chaos-to-clarity-monitoring-your-spring-boot-app-aa85bb500d4d)
      </br><a href="https://blog.devops.dev/from-chaos-to-clarity-monitoring-your-spring-boot-app-aa85bb500d4d"><img alt="Medium" src="https://img.shields.io/badge/Medium-12100E?style=for-the-badge&logo=medium&logoColor=white"></a>
>   - There's also a companion repo to the article here: [ahmedibrahimX/custom-mertics-demo](https://github.com/ahmedibrahimX/custom-metrics-demo)
> - A transaction logging table to contain the requests a user has made and its result. This can be used by the data team for analytics.
> - Add partitioning, and data retention strategy for the order table and for the transaction logging table (if added).
> - Add role based authorization (currently I only parse a jwt token to extract the name and the id from its claims, just for demonstration purpose)
> - Add rate limiting on the ordering API

:point_up: [Back to top](#table-of-contents)
