package it.unimi.cloudproject.bl.valueobjects;

import it.unimi.cloudproject.factories.bl.valueobjects.CoordinatesFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class CoordinatesTest {
    static Stream<Arguments> coordinatesConstructorSource() {
        return Stream.of(
                Arguments.arguments(-181, CoordinatesFactory.VALID_LATITUDE),
                Arguments.arguments(181, CoordinatesFactory.VALID_LATITUDE),
                Arguments.arguments(CoordinatesFactory.VALID_LONGITUDE, -91),
                Arguments.arguments(CoordinatesFactory.VALID_LONGITUDE, 91)
        );
    }

    @ParameterizedTest
    @MethodSource("coordinatesConstructorSource")
    void givenCoordinatesConstructor_whenParamsInvalid_thenThrow(double longitude, double latitude) {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new Coordinates(longitude, latitude));
    }
}
