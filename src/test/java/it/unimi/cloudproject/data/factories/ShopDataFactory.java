package it.unimi.cloudproject.data.factories;

import it.unimi.cloudproject.data.model.ShopData;
import it.unimi.cloudproject.data.model.UserData;
import it.unimi.cloudproject.data.repositories.ShopRepository;
import it.unimi.cloudproject.data.repositories.UserRepository;
import it.unimi.cloudproject.factories.bl.ShopFactory;
import it.unimi.cloudproject.factories.bl.UserFactory;

public class ShopDataFactory {
    public static ShopData createShop(ShopRepository shopRepo) {
        var shop = ShopFactory.getShop();
        var shopData = ShopData.fromShop(shop);
        return shopRepo.save(shopData);
    }
}
