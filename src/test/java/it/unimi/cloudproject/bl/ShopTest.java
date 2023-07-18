package it.unimi.cloudproject.bl;

import it.unimi.cloudproject.bl.errors.ValidationError;
import it.unimi.cloudproject.bl.valueobjects.Coordinates;
import it.unimi.cloudproject.factories.bl.ShopFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static it.unimi.cloudproject.factories.bl.ShopFactory.VALID_COORDINATES;
import static it.unimi.cloudproject.factories.bl.ShopFactory.VALID_SHOP_NAME;

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
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new Shop(ShopFactory.VALID_ID, name, coordinates));
    }

    @Test
    void givenShopConstructor_whenNameEmpty_thenThrow() {
        Assertions.assertThrows(ValidationError.EmptyNameForShopError.class, () ->
                new Shop(ShopFactory.VALID_ID, "", VALID_COORDINATES));
    }

    @Test
    void givenShopConstructor_whenParamsValid_thenOk() {
        var shop = ShopFactory.getShop();

        Assertions.assertEquals(ShopFactory.VALID_SHOP_NAME, shop.name());
        Assertions.assertEquals(VALID_COORDINATES, shop.coordinates());
    }
}
