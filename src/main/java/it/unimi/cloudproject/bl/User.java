package it.unimi.cloudproject.bl;

import it.unimi.cloudproject.infrastructure.utilities.StringUtils;

import java.util.Collection;

public record User(String username, Collection<Shop> favoriteShops) {
    public User {
        if (StringUtils.isNullOrEmpty(username))
            throw new IllegalArgumentException("Username cannot be null or empty");

        if (favoriteShops == null)
            throw new IllegalArgumentException("favoriteShops cannot be null");
    }
}
