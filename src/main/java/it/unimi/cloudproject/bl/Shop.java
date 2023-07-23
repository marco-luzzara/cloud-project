package it.unimi.cloudproject.bl;

import it.unimi.cloudproject.bl.errors.ValidationError;
import it.unimi.cloudproject.bl.valueobjects.Coordinates;
import it.unimi.cloudproject.infrastructure.annotations.codecoverage.Generated;

import java.util.Objects;

public record Shop(Integer id, String name, Coordinates coordinates) {
    public Shop {
        if (Objects.isNull(name))
            throw new IllegalArgumentException("name cannot be null");

        if (name.isEmpty())
            throw new ValidationError.EmptyNameForShopError();

        if (coordinates == null)
            throw new IllegalArgumentException("coordinates cannot be null");
    }

    @Generated
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shop shop = (Shop) o;
        return Objects.equals(name, shop.name) && Objects.equals(coordinates, shop.coordinates);
    }

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(name, coordinates);
    }
}
