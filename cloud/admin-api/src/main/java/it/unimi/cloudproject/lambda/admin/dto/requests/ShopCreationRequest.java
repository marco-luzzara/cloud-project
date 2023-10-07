package it.unimi.cloudproject.lambda.admin.dto.requests;

public record ShopCreationRequest(String name, int shopOwnerId, double longitude, double latitude) {
}
