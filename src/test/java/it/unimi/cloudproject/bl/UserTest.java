package it.unimi.cloudproject.bl;

import it.unimi.cloudproject.factories.bl.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.stream.Stream;

public class UserTest {
    static Stream<Arguments> userConstructorSource() {
        return Stream.of(
                Arguments.arguments(null, UserFactory.VALID_FAVORITE_SHOPS),
                Arguments.arguments("", UserFactory.VALID_FAVORITE_SHOPS),
                Arguments.arguments(UserFactory.VALID_USERNAME, null)
        );
    }

    @ParameterizedTest
    @MethodSource("userConstructorSource")
    void givenUserConstructor_whenParamsInvalid_thenThrow(String username, Collection<Shop> favoriteShops) {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new User(username, favoriteShops));
    }

    @Test
    void givenUserConstructor_whenNoFavoriteShops_thenOk() {
        var user = UserFactory.getUser();

        Assertions.assertEquals(0, user.favoriteShops().size());
    }
}
