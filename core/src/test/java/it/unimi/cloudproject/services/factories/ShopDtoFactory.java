package it.unimi.cloudproject.services.factories;

import it.unimi.cloudproject.services.dto.ShopCreation;
import it.unimi.cloudproject.factories.bl.ShopFactory;

public class ShopDtoFactory {
    public static ShopCreation createShopCreation() {
        var shop = ShopFactory.getShop();
        return new ShopCreation(shop.name(), shop.coordinates().longitude(), shop.coordinates().latitude());
    }

    public static ShopCreation createShopCreation(int seed) {
        var shop = ShopFactory.getShop(seed);
        return new ShopCreation(shop.name(), shop.coordinates().longitude(), shop.coordinates().latitude());
    }
}
