package it.unimi.cloudproject.data.repositories;

import it.unimi.cloudproject.factories.bl.ShopFactory;
import it.unimi.cloudproject.factories.data.ShopDataFactory;
import it.unimi.cloudproject.factories.data.UserDataFactory;
import it.unimi.cloudproject.data.model.ShopData;
import it.unimi.cloudproject.data.model.UserShopData;
import it.unimi.cloudproject.testutils.db.DbFactory;
import it.unimi.cloudproject.testutils.spring.DynamicPropertiesInjector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
public class ShopRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Container
    private static final PostgreSQLContainer<?> db = DbFactory.getPostgresContainer();

    @DynamicPropertySource
    static void dbProperties(DynamicPropertyRegistry registry) {
        DynamicPropertiesInjector.injectDatasourceFromPostgresContainer(registry, db);
    }

    @AfterEach
    void cleanupEach() {
        userRepository.deleteAll();
        shopRepository.deleteAll();
    }

    @Test
    void givenShop_whenAddIt_thenCanRetrieveIt() {
        var shopOwnerData = UserDataFactory.createUser(this.userRepository);
        var shopOwner = shopOwnerData.toUser();
        var shop = ShopFactory.getShop(shopOwner);
        var shopData = ShopData.fromShop(shop);
        shopData = shopRepository.save(shopData);

        var shopById = shopRepository.findById(shopData.getId()).stream().findFirst().orElseThrow();

        assertThat(shopById.getId()).isEqualTo(shopData.getId());
        assertThat(shopById).isEqualTo(shopData);
    }

    @Test
    void givenShop_whenFindByNameOrId_thenGetSameShop() {
        var shopOwnerData = UserDataFactory.createUser(this.userRepository);
        var shopOwner = shopOwnerData.toUser();
        var shop = ShopFactory.getShop(shopOwner);
        var shopData = ShopData.fromShop(shop);
        shopData = shopRepository.save(shopData);

        var shopByName = shopRepository.findById(shopData.getId()).stream().findFirst().orElseThrow();
        var shopById = shopRepository.findByName(shopData.getName()).stream().findFirst().orElseThrow();

        assertThat(shopByName).isEqualTo(shopById);
    }

    @Test
    void givenShop_whenDeleteIt_thenCannotRetrieveIt() {
        var shopOwnerData = UserDataFactory.createUser(this.userRepository);
        var shopData = ShopDataFactory.createShop(this.shopRepository, shopOwnerData);
        var shopId = shopData.getId();

        shopRepository.deleteById(shopId);

        assertThat(shopRepository.findById(shopId)).isEmpty();
    }

//    @Test
//    void givenZeroShopsForUser_whenGetFavoriteShopsOfUser_thenReturnsZero() {
//        var userData = UserDataFactory.createUser(this.userRepository);
//
//        var favoriteShops = shopRepository.findFavoriteShopsByUserId(userData.getId());
//
//        assertThat(favoriteShops).isEmpty();
//    }
//
//    @Test
//    void givenManyShopsForUser_whenGetFavoriteShopsOfUser_thenReturnsAll() {
//        var userData = UserDataFactory.createUser(this.userRepository);
//        var shopData1 = ShopDataFactory.createShop(this.shopRepository);
//        var shopData2 = ShopDataFactory.createShop(this.shopRepository);
//        userData.getFavoriteShops().addAll(List.of(
//                new UserShopData(AggregateReference.to(userData.getId()), AggregateReference.to(shopData1.getId())),
//                new UserShopData(AggregateReference.to(userData.getId()), AggregateReference.to(shopData2.getId()))));
//        userData = userRepository.save(userData);
//
//        var favoriteShops = shopRepository.findFavoriteShopsByUserId(userData.getId());
//
//        assertThat(favoriteShops).hasSize(2);
//        assertThat(favoriteShops).extracting(ShopData::getId).contains(shopData1.getId(), shopData2.getId());
//    }
}
