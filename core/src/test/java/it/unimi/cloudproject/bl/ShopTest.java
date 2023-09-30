package it.unimi.cloudproject.bl;

import it.unimi.cloudproject.bl.errors.ValidationError;
import it.unimi.cloudproject.bl.valueobjects.Coordinates;
import it.unimi.cloudproject.factories.bl.ShopFactory;
import it.unimi.cloudproject.factories.bl.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static it.unimi.cloudproject.factories.bl.ShopFactory.*;

public class ShopTest {
    static Stream<Arguments> shopConstructorSource() {
        return Stream.of(

                Arguments.arguments(null, VALID_COORDINATES),
                Arguments.arguments(VALID_SHOP_NAME, null)
        );
    }

    @ParameterizedTest
    @MethodSource("shopConstructorSource")
    void givenShopConstructor_whenParamsNull_thenThrow(String name, Coordinates coordinates) {
        var shopOwner = UserFactory.getUser();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new Shop(VALID_ID, shopOwner, name, coordinates));
    }

    @Test
    void givenShopConstructor_whenShopOwnerNull_thenThrow() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new Shop(VALID_ID, null, VALID_SHOP_NAME, VALID_COORDINATES));
    }

    @Test
    void givenShopConstructor_whenNameEmpty_thenThrow() {
        var shopOwner = UserFactory.getUser();

        Assertions.assertThrows(ValidationError.EmptyNameForShopError.class, () ->
                new Shop(VALID_ID, shopOwner, "", VALID_COORDINATES));
    }

    @Test
    void givenShopConstructor_whenParamsValid_thenOk() {
        var shopOwner = UserFactory.getUser();
        var shop = new Shop(VALID_ID, shopOwner, VALID_SHOP_NAME, VALID_COORDINATES);

        Assertions.assertEquals(ShopFactory.VALID_SHOP_NAME, shop.name());
        Assertions.assertEquals(shopOwner, shop.shopOwner());
        Assertions.assertEquals(VALID_COORDINATES, shop.coordinates());
    }
}
