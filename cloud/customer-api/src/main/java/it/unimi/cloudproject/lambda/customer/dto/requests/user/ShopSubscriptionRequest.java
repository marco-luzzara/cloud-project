package it.unimi.cloudproject.lambda.customer.dto.requests.user;

public record ShopSubscriptionRequest(String username, int userId, int shopId) {
}
