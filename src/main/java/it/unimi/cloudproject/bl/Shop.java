package it.unimi.cloudproject.bl;

import it.unimi.cloudproject.bl.errors.ValidationError;
import it.unimi.cloudproject.bl.valueobjects.Coordinates;
import it.unimi.cloudproject.infrastructure.utilities.StringUtils;

import java.util.Objects;

public record Shop(String name, Coordinates coordinates) {
    public Shop {
        if (Objects.isNull(name))
            throw new IllegalArgumentException("name cannot be null");

        if (name.isEmpty())
            throw new ValidationError.EmptyNameForShopError();

        if (coordinates == null)
            throw new IllegalArgumentException("coordinates cannot be null");
    }
}
