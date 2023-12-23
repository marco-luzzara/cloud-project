package it.unimi.cloudproject.factories.bl;

import it.unimi.cloudproject.bl.Shop;
import it.unimi.cloudproject.bl.User;
import it.unimi.cloudproject.bl.valueobjects.Coordinates;
import it.unimi.cloudproject.factories.bl.valueobjects.CoordinatesFactory;

public class ShopFactory {
    public static final String VALID_SHOP_NAME = "test_shop";
    public static final Coordinates VALID_COORDINATES = CoordinatesFactory.getCoordinates();
    public static final Integer VALID_ID = null;

    public static Shop getShop(User shopOwner) {
        return new Shop(VALID_ID, shopOwner, VALID_SHOP_NAME, VALID_COORDINATES);
    }
}
