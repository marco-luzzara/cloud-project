package it.unimi.cloudproject.bl;

import it.unimi.cloudproject.bl.valueobjects.Coordinates;
import it.unimi.cloudproject.infrastructure.utilities.StringUtils;

public record Shop(String name, Coordinates coordinates) {
    public Shop {
        if (StringUtils.isNullOrEmpty(name))
            throw new IllegalArgumentException("name cannot be null or empty");

        if (coordinates == null)
            throw new IllegalArgumentException("coordinates cannot be null");
    }
}
