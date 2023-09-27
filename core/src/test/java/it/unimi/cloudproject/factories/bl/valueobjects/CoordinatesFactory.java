package it.unimi.cloudproject.factories.bl.valueobjects;

import it.unimi.cloudproject.bl.valueobjects.Coordinates;

public class CoordinatesFactory {
    public static final double VALID_LONGITUDE = 0;
    public static final double VALID_LATITUDE = 0;

    public static Coordinates getCoordinates() {
        return new Coordinates(VALID_LONGITUDE, VALID_LATITUDE);
    }
}
