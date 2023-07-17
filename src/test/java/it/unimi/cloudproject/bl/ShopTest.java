package it.unimi.cloudproject.bl;

import it.unimi.cloudproject.bl.valueobjects.Coordinates;
import it.unimi.cloudproject.factories.bl.ShopFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static it.unimi.cloudproject.factories.bl.ShopFactory.*;

public class ShopTest {
    @Test
    void givenShopConstructor_whenParamsValid_thenOk() {
        var shop = ShopFactory.getShop();

        Assertions.assertEquals(ShopFactory.VALID_SHOP_NAME, shop.name());
        Assertions.assertEquals(VALID_COORDINATES, shop.coordinates());
    }

    static Stream<Arguments> shopConstructorSource() {
        return Stream.of(
                Arguments.arguments(null, VALID_COORDINATES),
                Arguments.arguments("", VALID_COORDINATES),
                Arguments.arguments(VALID_SHOP_NAME, null)
        );
    }

    @ParameterizedTest
    @MethodSource("shopConstructorSource")
    void givenShopConstructor_whenParamsInvalid_thenThrow(String name, Coordinates coordinates) {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new Shop(name, coordinates));
    }
}
