---
theme: dracula
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

# Demo Web App

Why: A user wants to be notified when a new product is released by his favorite shops

What: Scalable Web App based on REST APIs

How: with AWS Services

---

# Entities

There are 3 types of users:

<v-clicks>

- Customer - A generic user
- Shop - It releases new products
- Admin - Can turn a generic user account into a Shop account

</v-clicks>

---
layout: center
---

# Workflow #1: Shop Creation

<img src="/shop_creation.jpg" class="h-100 rounded shadow" />

---
layout: center
---

# Workflow #2: User Registration & Login

<img src="/user_signup_login.jpg" class="h-100 rounded shadow" />

---
layout: center
---

# Workflow #3: New Product Release

<img src="/pub_sub.jpg" class="h-100 rounded shadow" />

---

# Data Layer

Where to store Users and Shops info?

<img src="/db_diagram.jpg" class="h-35 rounded shadow" />

A relational database is suitable for the two tables and a single relationship

Solution with AWS? **RDS** (Relational Database Service)

- ✅ Consistency
- ✅ Easy scalability (by sharding on user id & shop owner id)
- ❌ What about subscriptions?

---

# User Subscriptions

*A user wants to be notified **WHEN** a new product is released by his favorite shops*

Requirements:

- Event-based system <mingcute-arrow-right-fill /> Every product released is an event  
- Pub/Sub system <mingcute-arrow-right-fill /> Events are selectively delivered

Solution with AWS? **SNS** (Simple Notification Service)

- ✅ publishers are not aware of subscribers, the broker takes care of that by using Topics (Shop ids)
- ✅ SNS notifications can be sent through emails (or processed by Lambda Functions for more complex scenarios)

---

# API Implementation

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

# Spring Cloud Function

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

# API Gateway

1. The user calls the API by sending an HTTP Request
2. ???
3. The corresponding Lambda Function is invoked

Solution with AWS? API Gateway

- ✅ Triggers a Lambda Function when the corresponding REST Api is called
- ✅ Automatic scaling based on the amount of traffic
- <bi-exclamation-square-fill class="text-yellow-400"/> Authentication & Authorization

---
layout: center
---

# Authentication & Authorization

<img src="/auth.png" class="h-120 rounded shadow" />

---

# Custom Authorization (v1)

What happens when an authenticated user calls an API?

1. The AWS API Gateway receives the request and tries to match the request with an existing API
2. The API Gateway contacts Amazon Cognito User Pools to validate the Auth token
3. The API Gateway contacts Amazon Cognito Federated Identities to temporarily give the authenticated user the privilege to call the APIs specified in its Role policy (IAM-based authorization)
4. The API Gateway verifies whether the policy actions include the requested API

<p />

- ✅ The APIs of each user group are protected
- ❌ A shop owner could send notifications for any shop

---

# Custom Authorization (v2)

New authorization workflow:

1. The AWS API Gateway receives the request and tries to match the request with an existing API
2. The API Gateway delegates the Auth part to a Custom Authorizer (a Lambda Function)
3. The Custom Authorizer checks the IAM roles assigned to the user group and the shop ownership if necessary 
4. The Custom Authorizer returns a policy document to the API Gateway
5. If the policy document allows the target API call, then the Lambda Function for the API is invoked

<p />

- ✅ More flexible authorization
- <bi-exclamation-square-fill class="text-yellow-400"/> More complex implementation

---
layout: center
---

# General Architecture

<img src="/architecture.jpg" class="h-100 rounded shadow" />

---

# IaC

Many services are involved <mingcute-arrow-right-fill /> Infrastructure setup is more and more convoluted

Solution? Infrastructure as Code

How?

- ❌ AWS CLI - Requires scripting, hard to maintain
- ✅ Terraform - Based on a declarative language (HCL) and efficient
  - Organization in modules
  - Idempotent

---

# Testing

- The basic functionalities, like CRUD operations on the database are straightforward to test
- But Lambda Functions with SNS? And the Custom Authorizer logic?

APIs can be directly tested on AWS on a testing environment, but...

- ❌ There are costs associated
- ❌ Slower development (especially if test-driven)
- ❌ DevOps pipeline? Regression tests? Test automation?

---

