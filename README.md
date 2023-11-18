# Sample Project for Local Development with Localstack

This is a sample project that shows how to use some AWS services to create a simple App, and test it using [Localstack](https://www.google.com/url?sa=t&source=web&cd=&cad=rja&uact=8&ved=2ahUKEwizos2yh7eBAxVhSPEDHRF2CU0QFnoECBEQAQ&url=https%3A%2F%2Flocalstack.cloud%2F&usg=AOvVaw3Y-_XGkVayRoxabMtF4Zzn&opi=89978449).

---

## App Description

The app involves customers and shops. A customer wants to be notified when a shop he/she is subscribed to has a new product to show.

A customer can subscribe to a shop using its email and it will receive an email whenever the shop publish a new message.

The shop is created by an Admin, that is previously contacted by a user, who requests to be the owner of the shop. An admin, can now approve the request and send messages on behalf of the shop. The communication between a user and and admin is supposed to happen by email, for instance, so it is not handled through the provided APIs.

Finally, users can delete their account and shop owners can delete the shops as well.

---

## User Authentication and Authorization

Users sign up and log in using AWS Cognito. When users are created, they are added to the customer user group. There are 3 user group in the user pool, and these groups correspond to the 3 possible roles in the application: customers, shops, admins. There is currently one admin that is automatically seeded in the user pool (and in the admin group). Every other user assumes the role of customer when signs up, but can gain the "shop" role if the admin creates a shop with him/her as the owner.

User groups are important to define the authorizations a certain user has when calls an API. Everytime a user logs in, he/she receives a OAuth2 token, that has to use in order to call the APIs that requires authentication. Among the claims in the token (both the `accessToken` or `idToken` are valid) the user group is automatically inserted by AWS Cognito. The authorizer, which is a Lambda Function, compares the requested API Arn with the ones that the associated user groups can access to and establish whether to allow or deny the request. A shop owner belongs to both the `customer` group and the `shop` group.

There is another claim in an access token: `dbId`. This claim maps the Cognito user to the corresponding user in the Database. In order to store the basic information about customers and shop, an instance of RDS is used. I have chosen a SQL db because there are just 2 tables and no complex relationships.

---

## APIs

The APIs are described using the Open Api Specification, open the `open-api.yml` file to see them. APIs are implemented in 3 Lambda functions, as many as the possible roles: admin, customer and shop. It is not a [mono-lambda APIs approach](https://aaronstuyvenberg.com/posts/monolambda-vs-individual-function-api) but neither a single-function APIs approach: it is a tradeoff that allows me to easily control the role authorization

TODO: api gateway

---



The API Gateway is configured for a Lambda Integration for each API called. In this case, for simplicity, I have used the [mono-lambda pattern](https://aaronstuyvenberg.com/posts/monolambda-vs-individual-function-api). It is particularly convenient because the application is built using the Spring Boot Framework, which takes care of request handling.

Shops can sign up with special permission, because they can publish notifications, contrary to normal users. For the notifications, I am using the AWS Simple Notification Service (SNS), and the notification is delivered through email, specified by the user at registration time.

---

## App Technical Aspects

### Spring Boot

The lambda that handles the API calls is a Spring Boot Application. Since the lambda is stateless by definition, I have used Liquibase to synchronize the Database changesets. Another useful dependency is `org.springframework.cloud:spring-cloud-function-adapter-aws`, that allows you to map a Java `Function/Consumer/Supplier` to a Lambda handler.

---

## Introduction to Localstack

There are two ways to run Localstack (for this project):

- **From the integration tests** - Testcontainer has a module for Localstack that allows to start it for the integration tests. It is particularly useful for the CI, that is taken care of by Github Actions.
- **From the Docker Compose** - There is a `docker-compose.yml` in the `scripts` folder. This compose file contains the Terraform configuration files to deploy the entire environment and the Localstack configurations as well. However, it must be run using the `start_local.sh` script, because the correct terraform variables must be injected.

  To make the deploy faster, two bind mounts are specified:
  - The Localstack persistence volume - In this way, Localstack persists all the changes applied to the emulated environment and the next start up will be faster
  - The Terraform state file - It reflects the current resources that Terraform has applied to the AWS environment

