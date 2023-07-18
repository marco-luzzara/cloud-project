package it.unimi.cloudproject.bl.valueobjects;

import it.unimi.cloudproject.factories.bl.valueobjects.CoordinatesFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class CoordinatesTest {
    static Stream<Arguments> invalidCoordinatesConstructorSource() {
        return Stream.of(
                Arguments.arguments(-181, CoordinatesFactory.VALID_LATITUDE),
                Arguments.arguments(181, CoordinatesFactory.VALID_LATITUDE),
                Arguments.arguments(CoordinatesFactory.VALID_LONGITUDE, -91),
                Arguments.arguments(CoordinatesFactory.VALID_LONGITUDE, 91)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidCoordinatesConstructorSource")
    void givenCoordinatesConstructor_whenParamsInvalid_thenThrow(double longitude, double latitude) {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new Coordinates(longitude, latitude));
    }

    static Stream<Arguments> validCoordinatesConstructorSource() {
        return Stream.of(
                Arguments.arguments(-180, CoordinatesFactory.VALID_LATITUDE),
                Arguments.arguments(180, CoordinatesFactory.VALID_LATITUDE),
                Arguments.arguments(CoordinatesFactory.VALID_LONGITUDE, -90),
                Arguments.arguments(CoordinatesFactory.VALID_LONGITUDE, 90),
                Arguments.arguments(120, CoordinatesFactory.VALID_LATITUDE),
                Arguments.arguments(CoordinatesFactory.VALID_LONGITUDE, -60)
        );
    }

    @ParameterizedTest
    @MethodSource("validCoordinatesConstructorSource")
    void givenCoordinatesConstructor_whenParamsValid_thenOk(double longitude, double latitude) {
        var coords = new Coordinates(longitude, latitude);

        Assertions.assertEquals(longitude, coords.longitude());
        Assertions.assertEquals(latitude, coords.latitude());
    }
}
