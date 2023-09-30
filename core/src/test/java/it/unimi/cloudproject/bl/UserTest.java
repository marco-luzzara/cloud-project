package it.unimi.cloudproject.bl;

import it.unimi.cloudproject.bl.errors.ValidationError;
import it.unimi.cloudproject.factories.bl.ShopFactory;
import it.unimi.cloudproject.factories.bl.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class UserTest {
    @ParameterizedTest
    @NullAndEmptySource
    void givenUserConstructor_whenParamsNull_thenThrow(String username) {
        Assertions.assertThrows(ValidationError.EmptyNameForUserError.class, () ->
                new User(UserFactory.VALID_ID, username));
    }

    @Test
    void givenUserConstructor_whenParamsValid_thenOk() {
        var user = UserFactory.getUser();

        Assertions.assertEquals(UserFactory.VALID_ID, user.id());
        Assertions.assertEquals(UserFactory.VALID_USERNAME, user.username());
    }
//
//    @Test
//    void givenUserConstructor_whenSomeFavoriteShops_thenOk() {
//        var user = UserFactory.getUser(ShopFactory.getShop());
//
//        Assertions.assertEquals(1, user.favoriteShops().size());
//    }

//    @Test
//    void givenUser_whenAddNullFavoriteShop_thenThrow() {
//        var user = UserFactory.getUser();
//
//        Assertions.assertThrows(IllegalArgumentException.class, () ->
//                user.addFavoriteShop(null));
//    }
//
//    @Test
//    void givenUser_whenAddDuplicateFavoriteShop_thenThrow() {
//        var user = UserFactory.getUser(ShopFactory.getShop());
//
//        Assertions.assertThrows(ValidationError.DuplicateShopForUserError.class, () ->
//                user.addFavoriteShop(ShopFactory.getShop()));
//    }
//
//    @Test
//    void givenUser_whenAddFavoriteShop_thenShopIsAdded() {
//        var user = UserFactory.getUser();
//
//        user.addFavoriteShop(ShopFactory.getShop());
//
//        Assertions.assertEquals(1, user.favoriteShops().size());
//    }
}
