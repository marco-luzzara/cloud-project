package it.unimi.cloudproject.bl;

import it.unimi.cloudproject.bl.errors.ValidationError;
import it.unimi.cloudproject.infrastructure.utilities.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public record User(String username, Collection<Shop> favoriteShops) {
    public User(String username, Collection<Shop> favoriteShops) {
        if (Objects.isNull(username))
            throw new IllegalArgumentException("Username cannot be null");

        if (username.isEmpty())
            throw new ValidationError.EmptyNameForUserError();

        if (favoriteShops == null || favoriteShops.stream().anyMatch(Objects::isNull))
            throw new IllegalArgumentException("favoriteShops cannot be null");

        this.username = username;
        this.favoriteShops = favoriteShops;

        checkForDuplicateShop();
    }

    private void checkForDuplicateShop() {
        var duplicateShops = CollectionUtils.findDuplicates(this.favoriteShops);
        if (duplicateShops.size() > 0)
            throw new ValidationError.DuplicateShopForUserError(this, duplicateShops.stream().toList().get(0));
    }

    public void addFavoriteShop(Shop shop) {
        if (Objects.isNull(shop))
            throw new IllegalArgumentException("shop cannot be null");

        this.favoriteShops.add(shop);

        checkForDuplicateShop();
    }

    @Override
    public Collection<Shop> favoriteShops() {
        return List.copyOf(this.favoriteShops);
    }
}
