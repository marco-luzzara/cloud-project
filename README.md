# Sample Project for Local Development with Localstack

This is a sample project that shows how to use some AWS services to create a simple App, and test it using [Localstack](https://www.google.com/url?sa=t&source=web&cd=&cad=rja&uact=8&ved=2ahUKEwizos2yh7eBAxVhSPEDHRF2CU0QFnoECBEQAQ&url=https%3A%2F%2Flocalstack.cloud%2F&usg=AOvVaw3Y-_XGkVayRoxabMtF4Zzn&opi=89978449).

---

## App Description

The app involves customers and shops. A customer wants to be notified when a shop he/she is subscribed to has a new product to show.

A customer can subscribe to a shop using its email and it will receive an email whenever the shop publishes a new message.

The shop is created by an Admin, that is previously contacted by a user, who requests to be the owner of the shop. An admin, can now approve the request and send messages on behalf of the shop. The communication between a user and admin is supposed to happen by email, for instance, so it is not handled through the provided APIs.

Finally, users can delete their account and shop owners can delete the shops as well.

---

## Project Structure

The project is a multi-module application, including the modules:

- `core`: contains the BL and the services that the API are going to call
- `code-coverage-report`: it is necessary to aggregate the code coverage reports from the other modules into a single report using the `jacoco-report-aggregation` plugin
- `cloud`:
  - `admin-api`: the Lambda Function for the Admin APIs
  - `apigateway-authorizer`: the Lambda Function that works as the custom authorizer for Cognito
  - `customer-api`: the Lambda Function for the Customer APIs
  - `infrastructure`: contains the terraform resources to setup the infrastructure and some utility classes for the Lambda modules
  - `initializer`: it is a Lambda function that is executed as soon as the Database is up and running. It creates the DB Tables.
  - `shop-api`: the Lambda Function for the Shop APIs
  - `test-api`: It contains the End-to-End tests of the APIs. This additional module is necessary because it tests many workflows and needs all the Lambda modules as dependencies.

---

## Technologies 

Many technologies are involved in this project:

### AWS

- **Lambda Functions**: API Implementation, Custom Authorizer logic and DB seeding
- **S3**: store the Lambda Functions code
- **Api Gateway**: Integrate APIs with Lambdas
- **CloudWatch**: For reading Lambda Logs and Metrics
- **RDS**: To store basic data about users and shops
- **Cognito**: for user authentication/authorization
- **SNS**: used to send notifications to users subscribed to a certain shop when it publishes a new message
- **Secrets Manager**: to store credentials (DB)
- **IAM**: to grant only the necessary privileges to the used services

### API Implementation

As for the API implementation, they are written in Java, with the extensive usage of Spring Boot. In particular for the Lambda definition, I have used Spring Cloud Function, that allows to code a Lambda Function as if it were a `java.util.Function`. Each Java module is then compiled using Gradle, but all the common tasks and dependencies are found in the local Gradle plugin located in `buildSrc`.

### Testing

Testing is a central point in this project, as I am showing how to run an emulation of AWS locally and in a CI pipeline. In order to do this, we need:

- **Docker**: with a `docker-compose.yml`
- **JUnit and Testcontainers**: for unit and integration tests
- **Jacoco** : to analyse the code coverage (which does not include the Lambda Functions coverage)
- **Localstack**: which is the platform that allows to build an emulation of AWS locally
- **Github Actions**: they are basically the CI pipelines. Two pipelines exist:
  - One for the feature branches that stops at the testing step and the only generated artifact is the code coverage report
  - One for the `main` branch where the coverage report and the APIs are published on Github Pages

### Others

The infrastructure is built using Terraform to define resources and some shell scripts, placed in the `scripts` folder. A Makefile contains all the commands to run the infrastructure with Localstack.

For the observability part, I have used OpenTelemetry APIs and the collector. Metrics are sent to Prometheus, while traces are sent to Jaeger. Grafana can then be used to query these data sources.

---

## User Authentication and Authorization

Users sign up and log in using AWS Cognito. When users are created, they are added to the customer user group. There are 3 user group in the user pool, and these groups correspond to the 3 possible roles in the application: customers, shops, admins. There is currently one admin that is automatically seeded in the user pool (and in the admin group). Every other user assumes the role of customer when signs up, but can gain the "shop" role if the admin creates a shop with him/her as the owner.

User groups are important to define the authorizations a certain user has when calls an API. Everytime a user logs in, he/she receives a OAuth2 token, that has to use in order to call the APIs that requires authentication. Among the claims in the token (both the `accessToken` or `idToken` are valid) the user group is automatically inserted by AWS Cognito. The authorizer, which is a Lambda Function, compares the requested API Arn with the ones that the associated user groups can access to and establish whether to allow or deny the request. A shop owner belongs to both the `customer` group and the `shop` group.

There is another claim in an access token: `dbId`. This claim maps the Cognito user to the corresponding user in the Database. In order to store the basic information about customers and shop, an instance of RDS is used. I have chosen a SQL db because there are just 2 tables and no complex relationships.

---

## APIs

The APIs are described using the Open Api Specification, open the `open-api.yml` file to see them. APIs are implemented in 3 Lambda functions, as many as the possible roles: admin, customer and shop. It is not a [mono-lambda APIs approach](https://aaronstuyvenberg.com/posts/monolambda-vs-individual-function-api) but neither a single-function APIs approach: it is a tradeoff that allows me to easily control the role authorization. In order to call the Lambda functions using HTTP Rest APIs, I needed the AWS Api Gateway. Each of the API is integrated with the corresponding Lambda function using an integration of type `AWS`. Also 4XX error cases are handled by creating different integration responses based on the `selection pattern`.

---

## Introduction to Localstack

There are two ways to run Localstack:

- **From the integration tests** - Testcontainer has a module for Localstack that allows to start it for the integration tests. It is particularly useful for the CI, that is taken care of by Github Actions.
- **From the Docker Compose** - The compose file spawns one container for Localstack and another for Terraform. Then, by running `make start` (or `make tf_apply` if already up), the localstack container is initialized with the defined resources.

  To make the deploy faster, two bind mounts are specified:
  - **The Localstack persistence volume** - In this way, Localstack persists all the changes applied to the emulated environment and the next start up will be faster
  - **The Terraform state files** - It reflects the current resources that Terraform has applied to the AWS environment

