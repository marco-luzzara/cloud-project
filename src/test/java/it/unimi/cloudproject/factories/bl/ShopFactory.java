package it.unimi.cloudproject.factories.bl;

import it.unimi.cloudproject.bl.Shop;
import it.unimi.cloudproject.bl.valueobjects.Coordinates;
import it.unimi.cloudproject.factories.bl.valueobjects.CoordinatesFactory;

public class ShopFactory {
    public static final String VALID_SHOP_NAME = "test_shop";
    public static final Coordinates VALID_COORDINATES = CoordinatesFactory.getCoordinates();
    public static final int VALID_ID = 0;

    public static Shop getShop() {
        return new Shop(VALID_ID, VALID_SHOP_NAME, VALID_COORDINATES);
    }
}
