package it.unimi.cloudproject.factories.services;

import it.unimi.cloudproject.data.model.UserData;
import it.unimi.cloudproject.services.dto.ShopCreation;
import it.unimi.cloudproject.factories.bl.ShopFactory;

public class ShopDtoFactory {
    public static ShopCreation createShopCreation(UserData shopOwnerData) {
        var shop = ShopFactory.getShop(shopOwnerData.toUser());
        return new ShopCreation(shop.name(), shopOwnerData.getId(), shop.coordinates().longitude(), shop.coordinates().latitude());
    }

//    public static ShopCreation createShopCreation(int seed) {
//        var shop = ShopFactory.getShop(seed);
//        return new ShopCreation(shop.name(), shop.coordinates().longitude(), shop.coordinates().latitude());
//    }
}
