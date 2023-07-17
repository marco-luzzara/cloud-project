package it.unimi.cloudproject.bl.valueobjects;

public record Coordinates(double longitude, double latitude) {
    public Coordinates {
        if (longitude < -180 || longitude > 180)
            throw new IllegalArgumentException("longitude boundaries are [-180, 180]");

        if (latitude < -90 || latitude > 90)
            throw new IllegalArgumentException("latitude boundaries are [-90, 90]");
    }
}
