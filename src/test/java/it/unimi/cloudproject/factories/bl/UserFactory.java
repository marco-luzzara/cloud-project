package it.unimi.cloudproject.factories.bl;

import it.unimi.cloudproject.bl.Shop;
import it.unimi.cloudproject.bl.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class UserFactory {
    public static final String VALID_USERNAME = "test_user";
    public static final Collection<Shop> VALID_FAVORITE_SHOPS = new ArrayList<>();
    public static final int VALID_ID = 0;

    public static User getUser() {
        return new User(VALID_ID, VALID_USERNAME, VALID_FAVORITE_SHOPS);
    }

    public static User getUser(Shop... shops) {
        return new User(VALID_ID, VALID_USERNAME, Arrays.stream(shops).collect(Collectors.toList()));
    }
}
