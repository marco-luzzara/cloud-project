package it.unimi.cloudproject.data.repositories;

import it.unimi.cloudproject.data.factories.ShopDataFactory;
import it.unimi.cloudproject.data.factories.UserDataFactory;
import it.unimi.cloudproject.data.model.ShopData;
import it.unimi.cloudproject.data.model.UserData;
import it.unimi.cloudproject.data.model.UserShopData;
import it.unimi.cloudproject.factories.bl.ShopFactory;
import it.unimi.cloudproject.factories.bl.UserFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ShopRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @AfterEach
    void cleanupEach() {
        userRepository.deleteAll();
        shopRepository.deleteAll();
    }

    @Test
    void givenShop_whenAddIt_thenCanRetrieveItByName() {
        var shop = ShopDataFactory.createShop(this.shopRepository);

        var shopByName = shopRepository.findByName(shop.getName()).stream().findFirst().orElseThrow();

        assertThat(shopByName.getId()).isEqualTo(shop.getId());
    }

    @Test
    void givenShop_whenDeleteIt_thenCannotRetrieveIt() {
        var shopData = ShopDataFactory.createShop(this.shopRepository);
        var shopId = shopData.getId();

        shopRepository.deleteById(shopId);

        assertThat(shopRepository.findById(shopId)).isEmpty();
    }

    @Test
    void givenZeroShopsForUser_whenGetFavoriteShopsOfUser_thenReturnsZero() {
        var userData = UserDataFactory.createUser(this.userRepository);

        var favoriteShops = shopRepository.findFavoriteShopsByUserId(userData.getId());

        assertThat(favoriteShops).isEmpty();
    }

    @Test
    void givenManyShopsForUser_whenGetFavoriteShopsOfUser_thenReturnsAll() {
        var userData = UserDataFactory.createUser(this.userRepository);
        var shopData1 = ShopDataFactory.createShop(this.shopRepository);
        var shopData2 = ShopDataFactory.createShop(this.shopRepository);
        userData.getFavoriteShops().addAll(List.of(
                new UserShopData(AggregateReference.to(userData.getId()), AggregateReference.to(shopData1.getId())),
                new UserShopData(AggregateReference.to(userData.getId()), AggregateReference.to(shopData2.getId()))));
        userData = userRepository.save(userData);

        var favoriteShops = shopRepository.findFavoriteShopsByUserId(userData.getId());

        assertThat(favoriteShops).hasSize(2);
        assertThat(favoriteShops).extracting(ShopData::getId).contains(shopData1.getId(), shopData2.getId());
    }
}
