package it.unimi.cloudproject.services;

import it.unimi.cloudproject.services.dto.ShopCreation;
import it.unimi.cloudproject.services.dto.ShopInfo;
import it.unimi.cloudproject.factories.services.ShopDtoFactory;
import it.unimi.cloudproject.factories.data.UserDataFactory;
import it.unimi.cloudproject.data.repositories.ShopRepository;
import it.unimi.cloudproject.data.repositories.UserRepository;
import it.unimi.cloudproject.factories.bl.ShopFactory;
import it.unimi.cloudproject.services.errors.InvalidShopIdError;
import it.unimi.cloudproject.services.errors.InvalidUserIdError;
import it.unimi.cloudproject.services.errors.UnauthorizedUserForShopError;
import it.unimi.cloudproject.services.services.ShopService;
import it.unimi.cloudproject.testutils.db.DbFactory;
import it.unimi.cloudproject.testutils.spring.DynamicPropertiesInjector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest
public class ShopServiceTest {
    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopService shopService;

    @Container
    private static final PostgreSQLContainer<?> db = DbFactory.getPostgresContainer();

    @DynamicPropertySource
    static void dbProperties(DynamicPropertyRegistry registry) {
        DynamicPropertiesInjector.injectDatasourceFromPostgresContainer(registry, db);
    }

    @AfterEach
    void cleanupEach() {
        shopRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void givenANewShop_whenSearchByItsName_thenReturnIt() {
        var shopOwner = UserDataFactory.createUser(this.userRepository);
        var shopCreationDto = ShopDtoFactory.createShopCreation(shopOwner);
        var shopId = shopService.addShop(shopCreationDto);

        var shops = shopService.findByName(shopCreationDto.name());

        assertThat(shops).hasSize(1);
        assertThat(shops.get(0)).returns(shopCreationDto.name(), from(ShopInfo::name))
                .returns(shopCreationDto.shopOwnerId(), from(ShopInfo::shopOwnerId))
                .returns(shopCreationDto.longitude(), from(ShopInfo::longitude))
                .returns(shopCreationDto.latitude(), from(ShopInfo::latitude))
                .returns(shopId, from(ShopInfo::id));
    }

    @Test
    void givenANewShop_whenOwnerDoesNotExist_thenThrow() {
        var shopCreationDto = new ShopCreation(ShopFactory.VALID_SHOP_NAME, 1000,
                ShopFactory.VALID_COORDINATES.longitude(), ShopFactory.VALID_COORDINATES.latitude());

        assertThatThrownBy(() -> shopService.addShop(shopCreationDto)).isInstanceOf(InvalidUserIdError.class);
    }

    @Test
    void givenAShop_whenDeleteItById_thenCannotRetrieveIt() {
        var shopOwner = UserDataFactory.createUser(this.userRepository);
        var shopCreationDto = ShopDtoFactory.createShopCreation(shopOwner);
        var shopId = shopService.addShop(shopCreationDto);

        shopService.deleteShop(shopId);

        assertThat(shopService.findByName(shopCreationDto.name())).isEmpty();
    }

    @Test
    void givenAShop_whenDeletedWithIncorrectId_thenThrow() {
        var shopOwner = UserDataFactory.createUser(this.userRepository);
        var shopCreationDto = ShopDtoFactory.createShopCreation(shopOwner);
        var shopId = shopService.addShop(shopCreationDto);

        assertThatThrownBy(() -> shopService.deleteShop(1000)).isInstanceOf(InvalidShopIdError.class);
    }

//    @Test
//    void givenManyShopsForUser_whenGetUserFavoriteShops_thenReturnThem() {
//        var userData = UserDataFactory.createUser(this.userRepository);
//        var shopData1 = ShopDataFactory.createShop(this.shopRepository);
//        var shopData2 = ShopDataFactory.createShop(this.shopRepository);
//        var shopData3 = ShopDataFactory.createShop(this.shopRepository);
//        userData.getFavoriteShops().addAll(List.of(
//                new UserShopData(AggregateReference.to(userData.getId()), AggregateReference.to(shopData1.getId())),
//                new UserShopData(AggregateReference.to(userData.getId()), AggregateReference.to(shopData2.getId()))));
//        userData = userRepository.save(userData);
//
//        var foundShops = shopService.getFavoriteShopsOfUser(userData.getId());
//
//        assertThat(foundShops).hasSize(2);
//        assertThat(foundShops).extracting(ShopInfo::id).containsExactly(shopData1.getId(), shopData2.getId());
//    }
}
