package it.unimi.cloudproject.services.services;

import it.unimi.cloudproject.data.repositories.UserRepository;
import it.unimi.cloudproject.infrastructure.errors.InternalException;
import it.unimi.cloudproject.services.dto.ShopCreation;
import it.unimi.cloudproject.services.dto.ShopInfo;
import it.unimi.cloudproject.bl.Shop;
import it.unimi.cloudproject.bl.valueobjects.Coordinates;
import it.unimi.cloudproject.data.model.ShopData;
import it.unimi.cloudproject.data.repositories.ShopRepository;
import it.unimi.cloudproject.services.errors.InvalidUserIdError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
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
        return this.shopRepository.findByName(name).stream().map(s ->
            new ShopInfo(s.getId(), s.getName(),
                    Optional.ofNullable(s.getShopOwner().getId()).
                            orElseThrow(() -> new InternalException("The shop owner for shop with id %d has been deleted"
                                    .formatted(s.getId()), new NullPointerException())),
            s.getCoordinates().longitude(), s.getCoordinates().latitude())).collect(Collectors.toList());
    }

    public void deleteShop(Integer shopId) {
        this.shopRepository.deleteById(shopId);
    }

//    public List<ShopInfo> getFavoriteShopsOfUser(int userId) {
//        return this.shopRepository.findFavoriteShopsByUserId(userId).stream()
//                .map(sd -> new ShopInfo(sd.getId(), sd.getName(),
//                        sd.getCoordinates().longitude(), sd.getCoordinates().latitude()))
//                .toList();
//    }
}
