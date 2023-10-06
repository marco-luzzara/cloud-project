package it.unimi.cloudproject.lambda.shop.dto.requests;

public record PublishMessageRequest(int userId, int shopId, String message) {
}
