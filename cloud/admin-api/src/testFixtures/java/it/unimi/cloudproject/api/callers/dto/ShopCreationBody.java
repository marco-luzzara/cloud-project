package it.unimi.cloudproject.api.callers.dto;

public record ShopCreationBody(String name, int shopOwnerId, float longitude, float latitude) {
}
