---
theme: default
class: text-center
highlighter: shikiji
lineNumbers: true
drawings:
  persist: false
transition: slide-left
title: Cloud Project Presentation
mdc: true
---

# AWS Testing with Localstack

---

## Demo Web App

Why: A user wants to be notified when a new product is released by his favorite shops

What: Scalable Web App based on REST APIs

How: with AWS Services

---

## Entities

There are 3 types of users:

<v-clicks>

- Customer - A generic user
- Shop - It releases new products
- Admin - Can turn a generic user account into a Shop account

</v-clicks>

---
layout: center
---

## Workflow #1: Shop Creation

<img src="/shop_creation.jpg" class="h-100 rounded shadow" />

---
layout: center
---

## Workflow #2: User Registration & Login

<img src="/user_signup_login.jpg" class="h-100 rounded shadow" />

---
layout: center
---

## Workflow #3: New Product Release

<img src="/pub_sub.jpg" class="h-100 rounded shadow" />

---

## Data Layer

Where to store Users and Shops info?

<img src="/db_diagram.jpg" class="h-35 rounded shadow" />

A relational database is suitable for the two tables and a single relationship

Solution with AWS? **RDS** (Relational Database Service)

- ✅ Consistency
- ✅ Easy scalability (by sharding on user id & shop owner id)
- ❌ What about subscriptions?

---

## User Subscriptions

*A user wants to be notified **WHEN** a new product is released by his favorite shops*

Requirements:

- Event-based system <mingcute-arrow-right-fill /> Every product released is an event  
- Pub/Sub system <mingcute-arrow-right-fill /> Events are selectively delivered

Solution with AWS? **SNS** (Simple Notification Service)

- ✅ publishers are not aware of subscribers, the broker takes care of that (no need for us to store them)
- ✅ SNS notifications can be sent through emails (or processed by Lambda Functions for more complex scenarios)

---

## API Implementation

The APIs are RESTful and stateless by design

- No state to manage <mingcute-arrow-right-fill /> Scalability
- Each API is like a function <mingcute-arrow-right-fill /> Easier implementation

Solution with AWS? Lambda Functions

- ✅ Serverless
- ✅ Independent Deployment

Implementation details:

- Language: Java 17
- Dependencies:
  - Spring (Spring Boot, Spring Data JDBC, Spring Cloud Function)
  - OpenTelemetry
  - ...

---

## Spring Cloud Function

```java {all|2,3|6|12|all}
@Bean
public MessageRoutingCallback customRouter() {
    return new MessageRoutingCallback() {
        @Override
        public String routingResult(Message<?> message) {
            return message.getHeaders().get("X-Spring-Cloud-Function-Definition");
        }
    };
}

@Bean
public Function<UserCreationRequest, UserCreationResponse> createUser() {
    return (userCreationRequest) -> {
        // ... Lambda implementation    
    };
}
```

---

## API Gateway

1. The user calls the API by sending an HTTP Request
2. ???
3. The corresponding Lambda Function is invoked

Solution with AWS? API Gateway

- 

---
layout: center
---

## Authentication & Authorization

<img src="/auth.png" class="h-120 rounded shadow" />

---

## Custom Authorization

---

