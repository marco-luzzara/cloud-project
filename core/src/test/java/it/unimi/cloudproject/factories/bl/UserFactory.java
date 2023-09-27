package it.unimi.cloudproject.factories.bl;

import it.unimi.cloudproject.bl.Shop;
import it.unimi.cloudproject.bl.User;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UserFactory {
    public static final String VALID_USERNAME = "test_user";
    public static final Set<Shop> VALID_FAVORITE_SHOPS = new HashSet<>();
    public static final Integer VALID_ID = null;

    public static User getUser() {
        return new User(VALID_ID, VALID_USERNAME, VALID_FAVORITE_SHOPS);
    }

    public static User getUser(Shop... shops) {
        return new User(VALID_ID, VALID_USERNAME, Arrays.stream(shops).collect(Collectors.toSet()));
    }
}
