package it.unimi.cloudproject.factories.bl;

import it.unimi.cloudproject.bl.Shop;
import it.unimi.cloudproject.bl.User;
import it.unimi.cloudproject.bl.valueobjects.Coordinates;
import it.unimi.cloudproject.factories.bl.valueobjects.CoordinatesFactory;

import java.util.Collection;
import java.util.List;

public class UserFactory {
    public static final String VALID_USERNAME = "test_user";
    public static final Collection<Shop> VALID_FAVORITE_SHOPS = List.of();

    public static User getUser() {
        return new User(VALID_USERNAME, VALID_FAVORITE_SHOPS);
    }
}
