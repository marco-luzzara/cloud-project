package it.unimi.cloudproject.bl;

import it.unimi.cloudproject.bl.errors.ValidationError;
import it.unimi.cloudproject.infrastructure.annotations.codecoverage.Generated;
import it.unimi.cloudproject.infrastructure.utilities.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record User(Integer id, String username, Set<Shop> favoriteShops) {
    public User {
        if (Objects.isNull(username))
            throw new IllegalArgumentException("Username cannot be null");

        if (username.isEmpty())
            throw new ValidationError.EmptyNameForUserError();

        if (favoriteShops == null || favoriteShops.stream().anyMatch(Objects::isNull))
            throw new IllegalArgumentException("favoriteShops cannot be null");
    }

    public void addFavoriteShop(Shop shop) {
        if (Objects.isNull(shop))
            throw new IllegalArgumentException("shop cannot be null");

        var beforeAddSize = this.favoriteShops.size();
        this.favoriteShops.add(shop);

        if (this.favoriteShops.size() == beforeAddSize)
            throw new ValidationError.DuplicateShopForUserError(this, shop);
    }

    @Override
    public Set<Shop> favoriteShops() {
        return Set.copyOf(this.favoriteShops);
    }

    @Generated
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
