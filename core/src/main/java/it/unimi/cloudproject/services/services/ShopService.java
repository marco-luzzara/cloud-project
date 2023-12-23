package it.unimi.cloudproject.services.services;

import it.unimi.cloudproject.bl.Shop;
import it.unimi.cloudproject.bl.valueobjects.Coordinates;
import it.unimi.cloudproject.data.model.ShopData;
import it.unimi.cloudproject.data.repositories.ShopRepository;
import it.unimi.cloudproject.data.repositories.UserRepository;
import it.unimi.cloudproject.infrastructure.errors.InternalException;
import it.unimi.cloudproject.services.dto.ShopCreation;
import it.unimi.cloudproject.services.dto.ShopInfo;
import it.unimi.cloudproject.services.errors.InvalidShopIdError;
import it.unimi.cloudproject.services.errors.InvalidUserIdError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShopService {
    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    public int addShop(ShopCreation shopCreation) {
        var shopOwnerId = shopCreation.shopOwnerId();
        var shopOwner = userRepository.findById(shopOwnerId).orElseThrow(() -> new InvalidUserIdError(shopOwnerId)).toUser();
        var shop = new Shop(null,
                shopOwner,
                shopCreation.name(),
                new Coordinates(shopCreation.longitude(), shopCreation.latitude()));

        var createdShop = this.shopRepository.save(ShopData.fromShop(shop));

        return createdShop.getId();
    }

    public List<ShopInfo> findByName(String name) {
        return this.shopRepository.findByName(name).stream().map(this::getShopInfoFromData)
                .collect(Collectors.toList());
    }

    public ShopInfo findById(int shopId) {
        return this.shopRepository.findById(shopId).stream().findFirst().map(this::getShopInfoFromData)
                .orElseThrow(() -> new InvalidShopIdError(shopId));
    }

    public void deleteShop(int shopId) {
        this.findById(shopId); // if the shop with shopId does not exist then error is thrown
        this.shopRepository.deleteById(shopId);
    }

    private ShopInfo getShopInfoFromData(ShopData shopData) {
        return new ShopInfo(shopData.getId(), shopData.getName(),
                Optional.ofNullable(shopData.getShopOwner().getId()).
                        orElseThrow(() -> new InternalException("The shop owner for shop with id %d has been deleted"
                                .formatted(shopData.getId()), new NullPointerException())),
                shopData.getCoordinates().longitude(), shopData.getCoordinates().latitude());
    }
}
