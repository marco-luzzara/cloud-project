package it.unimi.cloudproject.lambda.dto.requests.shop;

public record ShopCreationRequest(String name, int shopOwnerId, double longitude, double latitude) {
}
