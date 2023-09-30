package it.unimi.cloudproject.factories.data;

import it.unimi.cloudproject.data.model.ShopData;
import it.unimi.cloudproject.data.model.UserData;
import it.unimi.cloudproject.data.repositories.ShopRepository;
import it.unimi.cloudproject.factories.bl.ShopFactory;

public class ShopDataFactory {
    public static ShopData createShop(ShopRepository shopRepo, UserData shopOwnerData) {
        var shopOwner = shopOwnerData.toUser();
        var shop = ShopFactory.getShop(shopOwner);
        var shopData = ShopData.fromShop(shop);
        return shopRepo.save(shopData);
    }
}
