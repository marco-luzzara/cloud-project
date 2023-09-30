package it.unimi.cloudproject.services;

import it.unimi.cloudproject.services.dto.UserInfo;
import it.unimi.cloudproject.factories.services.UserDtoFactory;
import it.unimi.cloudproject.bl.errors.ValidationError;
import it.unimi.cloudproject.factories.data.ShopDataFactory;
import it.unimi.cloudproject.factories.data.UserDataFactory;
import it.unimi.cloudproject.data.model.ShopData;
import it.unimi.cloudproject.data.model.UserShopData;
import it.unimi.cloudproject.data.repositories.ShopRepository;
import it.unimi.cloudproject.data.repositories.UserRepository;
import it.unimi.cloudproject.factories.bl.UserFactory;
import it.unimi.cloudproject.services.services.UserService;
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

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest
public class UserServiceTest {
    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

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
    void givenCreatedUser_whenGetById_thenRetrieveIt() {
        var id = userService.addUser(UserDtoFactory.createUserCreationData());

        var userInfo = userService.getUser(id);

        assertThat(userInfo.orElseThrow()).returns(id, from(UserInfo::id))
                .returns(UserFactory.VALID_USERNAME, from(UserInfo::username));
    }

    @Test
    void givenUser_whenDeleteIt_thenCannotRetrieveItById() {
        var id = userService.addUser(UserDtoFactory.createUserCreationData());

        userService.deleteUser(id);

        var userInfo = userService.getUser(id);
        assertThat(userInfo).isEmpty();
    }

//    @Test
//    void givenManyUsersSubscribedToShop_whenGetUsersByShop_thenReturnThem() {
//        var shopData = ShopDataFactory.createShop(this.shopRepository);
//        var userData1 = UserDataFactory.createUser(this.userRepository);
//        var userData2 = UserDataFactory.createUser(this.userRepository);
//        UserDataFactory.createUser(this.userRepository);
//        userData1.getFavoriteShops().add(
//                new UserShopData(AggregateReference.to(userData1.getId()), AggregateReference.to(shopData.getId())));
//        userData2.getFavoriteShops().add(
//                new UserShopData(AggregateReference.to(userData2.getId()), AggregateReference.to(shopData.getId())));
//        userRepository.saveAll(List.of(userData1, userData2));
//
//        var users = userService.getUsersSubscribedToShop(shopData.getId());
//
//        assertThat(users).hasSize(2)
//                .extracting(UserInfo::id).contains(userData1.getId(), userData2.getId());
//    }
//
//    @Test
//    void givenNewShopForUser_whenAddToFavorite_thenAddIt() {
//        var userData = UserDataFactory.createUser(this.userRepository);
//        var shopData = ShopDataFactory.createShop(this.shopRepository);
//
//        userService.addShopToFavorite(userData.getId(), shopData.getId());
//
//        assertThat(shopRepository.findFavoriteShopsByUserId(userData.getId())).extracting(ShopData::getId)
//                .containsExactly(shopData.getId());
//    }
//
//    @Test
//    void givenDuplicateShopForUser_whenAddToFavorite_thenThrow() {
//        var userData = UserDataFactory.createUser(this.userRepository);
//        var shopData1 = ShopDataFactory.createShop(this.shopRepository);
//        var shopData2 = ShopDataFactory.createShop(this.shopRepository);
//
//        userService.addShopToFavorite(userData.getId(), shopData1.getId());
//
//        assertThatThrownBy(() ->
//                userService.addShopToFavorite(userData.getId(), shopData2.getId())
//        ).isInstanceOf(ValidationError.DuplicateShopForUserError.class);
//    }
}
