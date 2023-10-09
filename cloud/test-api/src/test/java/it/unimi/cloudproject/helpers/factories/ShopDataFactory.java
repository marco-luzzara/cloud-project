package it.unimi.cloudproject.helpers.factories;

import it.unimi.cloudproject.api.callers.dto.ShopCreationBody;
import it.unimi.cloudproject.lambda.customer.dto.requests.user.UserCreationRequest;

import java.util.Random;

public class ShopDataFactory {
    private static int shopCounter = 0;
    private static final Random random = new Random();
    public static ShopCreationBody getNewShop(int ownerId) {
        shopCounter++;
        return new ShopCreationBody("shop" + shopCounter, ownerId, random.nextFloat() * 90, random.nextFloat() * 90);
    }
}
