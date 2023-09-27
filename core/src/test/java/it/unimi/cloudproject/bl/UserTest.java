package it.unimi.cloudproject.bl;

import it.unimi.cloudproject.bl.errors.ValidationError;
import it.unimi.cloudproject.factories.bl.ShopFactory;
import it.unimi.cloudproject.factories.bl.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class UserTest {
    static Stream<Arguments> userConstructorSource() {
        Set<Shop> invalidShops = new HashSet<>();
        invalidShops.add(ShopFactory.getShop());
        invalidShops.add(null);

        return Stream.of(
                Arguments.arguments(null, UserFactory.VALID_FAVORITE_SHOPS),
                Arguments.arguments(UserFactory.VALID_USERNAME, null),
                Arguments.arguments(UserFactory.VALID_USERNAME, invalidShops)
        );
    }

    @ParameterizedTest
    @MethodSource("userConstructorSource")
    void givenUserConstructor_whenParamsNull_thenThrow(String username, Set<Shop> favoriteShops) {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new User(UserFactory.VALID_ID, username, favoriteShops));
    }

    @Test
    void givenUserConstructor_whenNameEmpty_thenThrow() {
        Assertions.assertThrows(ValidationError.EmptyNameForUserError.class, () ->
                new User(UserFactory.VALID_ID, "", UserFactory.VALID_FAVORITE_SHOPS));
    }

    @Test
    void givenUserConstructor_whenNoFavoriteShops_thenOk() {
        var user = UserFactory.getUser();

        Assertions.assertEquals(UserFactory.VALID_ID, user.id());
        Assertions.assertEquals(UserFactory.VALID_USERNAME, user.username());
        Assertions.assertEquals(0, user.favoriteShops().size());
    }

    @Test
    void givenUserConstructor_whenSomeFavoriteShops_thenOk() {
        var user = UserFactory.getUser(ShopFactory.getShop());

        Assertions.assertEquals(1, user.favoriteShops().size());
    }

    @Test
    void givenUser_whenAddNullFavoriteShop_thenThrow() {
        var user = UserFactory.getUser();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                user.addFavoriteShop(null));
    }

    @Test
    void givenUser_whenAddDuplicateFavoriteShop_thenThrow() {
        var user = UserFactory.getUser(ShopFactory.getShop());

        Assertions.assertThrows(ValidationError.DuplicateShopForUserError.class, () ->
                user.addFavoriteShop(ShopFactory.getShop()));
    }

    @Test
    void givenUser_whenAddFavoriteShop_thenShopIsAdded() {
        var user = UserFactory.getUser();

        user.addFavoriteShop(ShopFactory.getShop());

        Assertions.assertEquals(1, user.favoriteShops().size());
    }
}
