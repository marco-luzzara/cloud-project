package it.unimi.cloudproject.application.services;

import it.unimi.cloudproject.application.dto.ShopCreation;
import it.unimi.cloudproject.application.dto.ShopInfo;
import it.unimi.cloudproject.application.factories.ShopDtoFactory;
import it.unimi.cloudproject.data.factories.ShopDataFactory;
import it.unimi.cloudproject.data.factories.UserDataFactory;
import it.unimi.cloudproject.data.model.UserShopData;
import it.unimi.cloudproject.data.repositories.ShopRepository;
import it.unimi.cloudproject.data.repositories.UserRepository;
import it.unimi.cloudproject.factories.bl.ShopFactory;
import it.unimi.cloudproject.factories.bl.valueobjects.CoordinatesFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

@SpringBootTest
@ActiveProfiles("test")
public class ShopServiceTest {
    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopService shopService;

    @AfterEach
    void cleanupEach() {
        shopRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void givenAShop_whenSearchByItsName_thenReturnIt() {
        var shopId = shopService.addShop(ShopDtoFactory.createShopCreation());

        var shops = shopService.findByName(ShopFactory.VALID_SHOP_NAME);

        assertThat(shops).hasSize(1);
        assertThat(shops.get(0)).returns(ShopFactory.VALID_SHOP_NAME, from(ShopInfo::name))
                .returns(ShopFactory.VALID_COORDINATES.longitude(), from(ShopInfo::longitude))
                .returns(ShopFactory.VALID_COORDINATES.latitude(), from(ShopInfo::latitude))
                .returns(shopId, from(ShopInfo::id));
    }

    @Test
    void givenAShop_whenDeleteItById_thenCannotRetrieveIt() {
        var shopId = shopService.addShop(ShopDtoFactory.createShopCreation());

        shopService.deleteShop(shopId);

        assertThat(shopService.findByName(ShopFactory.VALID_SHOP_NAME)).isEmpty();
    }

    @Test
    void givenManyShopsForUser_whenGetUserFavoriteShops_thenReturnThem() {
        var userData = UserDataFactory.createUser(this.userRepository);
        var shopData1 = ShopDataFactory.createShop(this.shopRepository);
        var shopData2 = ShopDataFactory.createShop(this.shopRepository);
        var shopData3 = ShopDataFactory.createShop(this.shopRepository);
        userData.getFavoriteShops().addAll(List.of(
                new UserShopData(AggregateReference.to(userData.getId()), AggregateReference.to(shopData1.getId())),
                new UserShopData(AggregateReference.to(userData.getId()), AggregateReference.to(shopData2.getId()))));
        userData = userRepository.save(userData);

        var foundShops = shopService.getFavoriteShopsOfUser(userData.getId());

        assertThat(foundShops).hasSize(2);
        assertThat(foundShops).extracting(ShopInfo::id).containsExactly(shopData1.getId(), shopData2.getId());
    }
}
