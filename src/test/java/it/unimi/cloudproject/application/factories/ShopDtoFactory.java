package it.unimi.cloudproject.application.factories;

import it.unimi.cloudproject.application.dto.ShopCreation;
import it.unimi.cloudproject.data.model.ShopData;
import it.unimi.cloudproject.data.repositories.ShopRepository;
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
