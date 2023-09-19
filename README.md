# Sample Project for Local Development with Localstack

This is a sample project that shows how to use some AWS services to create a simple App, and test it using [Localstack](https://www.google.com/url?sa=t&source=web&cd=&cad=rja&uact=8&ved=2ahUKEwizos2yh7eBAxVhSPEDHRF2CU0QFnoECBEQAQ&url=https%3A%2F%2Flocalstack.cloud%2F&usg=AOvVaw3Y-_XGkVayRoxabMtF4Zzn&opi=89978449).

---

## App Description

The app involves customers and shops. A customer wants to be notified when a shop he/she is subscribed has a new product to show.

Customers are users that sign up using AWS Cognito, and can manage their profile information using REST API, callable through an AWS API Gateway. The API Gateway is configured for a Lambda Integration for each API called. In this case, for simplicity, I have used the [mono-lambda pattern](https://aaronstuyvenberg.com/posts/monolambda-vs-individual-function-api). It is particularly convenient because the application is built using the Spring Boot Framework, which takes care of request handling.

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

