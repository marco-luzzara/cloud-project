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

<v-clicks>

- Why: A user wants to be notified when a new product is released by his favorite shops

- What: Scalable Web App based on REST APIs

- How: With AWS Services

</v-clicks>

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

<div v-click>

Where to store Users and Shops info?

<img src="/db_diagram.jpg" class="h-35 rounded shadow" />

A relational database is suitable for the two tables and a single relationship

</div>

<v-click>

Solution with AWS? **RDS** (Relational Database Service)

</v-click>

<v-clicks>

- ✅ Consistency
- ✅ Easy scalability (by sharding on user id & shop owner id)
- ❌ What about subscriptions?

</v-clicks>

---

# User Subscriptions

*A user wants to be notified **WHEN** a new product is released by his favorite shops*

Requirements:

<v-clicks>

- Event-based system <mingcute-arrow-right-fill /> Every product released is an event
- Pub/Sub architecture <mingcute-arrow-right-fill /> Events are selectively delivered

</v-clicks>

<v-click>

Solution with AWS? **SNS** (Simple Notification Service)

</v-click>

<v-clicks>

- ✅ publishers are not aware of subscribers, the broker takes care of that by using Topics (Shop ids)
- ✅ SNS notifications can be sent through emails (or processed by Lambda Functions for more complex scenarios)

</v-clicks>

---

# API Implementation

The APIs are RESTful and stateless by design

<v-clicks>

- No state to manage <mingcute-arrow-right-fill /> Scalability
- Each API is like a function <mingcute-arrow-right-fill /> Easier implementation

</v-clicks>

<v-click>

Solution with AWS? **Lambda Functions**

</v-click>

<v-clicks>

- ✅ Serverless
- ✅ Independent Deployment

</v-clicks>

<div v-click>

Implementation details:

- Language: Java 17
- Dependencies:
  - Spring (Spring Boot, Spring Data JDBC, Spring Cloud Function)
  - OpenTelemetry
  - ...

</div>

---

# Spring Cloud Function

```java {all|1-3,8,9|4-7|11-16|all}
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

<div v-click>

1. The user calls the API by sending an HTTP Request
2. ???
3. The corresponding Lambda Function is invoked

</div>

<v-click>

Solution with AWS? **API Gateway**

</v-click>

<v-clicks>

- ✅ Triggers a Lambda Function when the corresponding REST Api is called
- ✅ Automatic scaling based on the amount of traffic
- <bi-exclamation-square-fill class="text-yellow-400"/> Authentication & Authorization

</v-clicks>

---
layout: center
---

# Authentication & Authorization

<img src="/auth.png" class="h-120 rounded shadow" />

---

# Custom Authorization (v1)

What happens when an authenticated user calls an API?

<v-clicks>

1. The AWS API Gateway receives the request and tries to match the request with an existing API
2. The API Gateway contacts Amazon Cognito User Pool Client to validate the Auth token
3. The API Gateway contacts Amazon Cognito Federated Identities to temporarily give the authenticated user the privilege to call the APIs specified in its Role policy (IAM-based authorization)
4. The API Gateway verifies whether the policy actions include the requested API

</v-clicks>

<p />

<v-clicks>

- ✅ The APIs of each user group are protected
- ❌ A shop owner could send notifications for any shop

</v-clicks>

---

# Custom Authorization (v2)

New authorization workflow:

<v-clicks>

1. The AWS API Gateway receives the request and tries to match the request with an existing API
2. The API Gateway delegates the Auth part to a Custom Authorizer (a Lambda Function)
3. The Custom Authorizer checks the IAM roles assigned to the user group and the shop ownership if necessary 
4. The Custom Authorizer returns a policy document to the API Gateway
5. If the policy document allows the target API call, then the Lambda Function for the API is invoked

</v-clicks>

<p />

<v-clicks>

- ✅ More flexible authorization
- <bi-exclamation-square-fill class="text-yellow-400"/> More complex implementation

</v-clicks>

---
layout: center
---

# General Architecture

<img src="/architecture.jpg" class="h-100 rounded shadow" />

---

# IaC

The infrastructure is getting bigger (almost 100 resources)...

<v-clicks>

- Many services are involved <mingcute-arrow-right-fill /> Infrastructure setup is more and more convoluted
- Manually deploying would not be efficient because resources are sequentially provisioned
- Version control? Reproducibility?

</v-clicks>

<div v-click>

Solution? **Infrastructure as Code**

How?

</div>

<v-clicks>

- ❌ AWS CLI - Requires scripting, hard to maintain
- ✅ Terraform - Based on a declarative language (HCL) and efficient

</v-clicks>

---

# Testing

<v-clicks>

- The basic functionalities, like CRUD operations on the database are straightforward to test
- But Lambda Functions with SNS? And the Custom Authorizer logic?

</v-clicks>

<v-click>

APIs can be directly tested on AWS on a testing environment, but...

</v-click>

<v-clicks>

- ❌ There are costs associated
- ❌ Slower development (especially if test-driven)
- ❌ DevOps pipeline? Regression tests? Test automation?

</v-clicks>

<v-click>

Solution? **Deploying locally**

</v-click>

---

# Localstack

*"LocalStack is a cloud service emulator that runs in a single container on your laptop or in your CI environment"*

<v-clicks>

- Available as a Docker container (port 4566)
- Localstack and AWS APIs' are the same
- Integrates with Terraform
- Speeds up development thanks to features such as Lambda Function Hot-Reloading
- 0 costs

</v-clicks>

---

# Localstack in a Test Pipeline

Localstack proves valuable for Lambda Functions (API) testing during development, but a test pipeline should not assume the presence of any pre-existing container

<v-click>

Solution? **Testcontainers**

</v-click>

<v-click>

*"Testcontainers is a library that provides easy and lightweight APIs for bootstrapping local development and test dependencies with real services wrapped in Docker containers."*

</v-click>

<v-clicks>

- ✅ Localstack setup/teardown can be managed by Testcontainers with its Module
- ❌ IaC for resource provisioning? No Testcontainers module for Terraform

</v-clicks>

<v-click>

Testcontainers is extendable with custom modules...

</v-click>

---
layout: center
---

# Deploying on Localstack

<img src="/deployment_process.png" class="h-100 rounded shadow" />

---

# CI with Github Actions

Localstack Pro can be integrated into a CI pipeline, but limited by a credit system

There are two types of pipelines:

<v-clicks>

1. Commit on a feature branch - Run tests and publish the coverage as artifact
2. Commit on a main branch - Same as #1, but also publishes the coverage and the APIs documentation on Github Pages

</v-clicks>

<v-click>

❌ Running Localstack tests on Github Actions every time there is a commit would deplete the credits...

</v-click>

<div v-click>

Solution? Execute tests exclusively when

```plain
startsWith(github.event.head_commit.message, '[ITENABLED]') || 
  github.event.inputs.integrationTestsEnabled == 'true'
```

</div>

---
layout: center
---

# Observability (#1)

<img src="/observability.png" class="h-100 rounded shadow" />

---

# Observability (#2)

Officially, Localstack does not support tracing <mingcute-arrow-right-fill /> The Lambda Function Runtime sometimes fails

Two types of instrumentation:

<v-clicks>

- Auto-Instrumentation with the `-javaagent:/var/task/lib/aws-opentelemetry-agent.jar`
- Manual Instrumentation (for custom metrics and tracing)
  - `@WithSpan` annotated methods represent new spans
  - OpenTelemetry APIs for generating new metrics 

</v-clicks>

---

# Localstack Pros & Cons

<div class="v-lane">

### Pros ✅

<v-clicks>

- Rapid development (and potentially TDD) within an environment mirroring AWS
- Growing community of contributors
- Ad-hoc features for the local development

</v-clicks>

</div>

<div class="v-lane" style="left: 50%">

### Cons ❌

<v-clicks>

- Some APIs are not implemented yet
- AWS != Localstack <mingcute-arrow-right-fill /> Checkpoint on AWS are recommended
- Limited CI (100 pipelines per month with the educational license)

</v-clicks>

</div>