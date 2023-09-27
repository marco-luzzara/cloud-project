package it.unimi.cloudproject.services.services;

import it.unimi.cloudproject.services.dto.ShopCreation;
import it.unimi.cloudproject.services.dto.ShopInfo;
import it.unimi.cloudproject.bl.Shop;
import it.unimi.cloudproject.bl.valueobjects.Coordinates;
import it.unimi.cloudproject.data.model.ShopData;
import it.unimi.cloudproject.data.repositories.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShopService {
    @Autowired
    private ShopRepository shopRepository;

    public int addShop(ShopCreation shopCreation) {
        var shop = new Shop(null,
                shopCreation.name(),
                new Coordinates(shopCreation.longitude(), shopCreation.latitude()));

        var createdShop = this.shopRepository.save(ShopData.fromShop(shop));

        return createdShop.getId();
    }

    public List<ShopInfo> findByName(String name) {
        return this.shopRepository.findByName(name).stream().map(u ->
                new ShopInfo(u.getId(), u.getName(), u.getCoordinates().longitude(), u.getCoordinates().latitude()))
                .collect(Collectors.toList());
    }

    public void deleteShop(Integer shopId) {
        this.shopRepository.deleteById(shopId);
    }

    public List<ShopInfo> getFavoriteShopsOfUser(int userId) {
        return this.shopRepository.findFavoriteShopsByUserId(userId).stream()
                .map(sd -> new ShopInfo(sd.getId(), sd.getName(),
                        sd.getCoordinates().longitude(), sd.getCoordinates().latitude()))
                .toList();
    }
}
