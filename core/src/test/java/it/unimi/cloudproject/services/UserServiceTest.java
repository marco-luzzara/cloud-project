package it.unimi.cloudproject.services;

import it.unimi.cloudproject.data.repositories.ShopRepository;
import it.unimi.cloudproject.data.repositories.UserRepository;
import it.unimi.cloudproject.factories.bl.UserFactory;
import it.unimi.cloudproject.factories.data.UserDataFactory;
import it.unimi.cloudproject.factories.services.ShopDtoFactory;
import it.unimi.cloudproject.factories.services.UserDtoFactory;
import it.unimi.cloudproject.services.dto.UserInfo;
import it.unimi.cloudproject.services.errors.InvalidShopIdError;
import it.unimi.cloudproject.services.errors.InvalidUserIdError;
import it.unimi.cloudproject.services.services.ShopService;
import it.unimi.cloudproject.services.services.UserService;
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
public class UserServiceTest {
    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

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
    void givenCreatedUser_whenGetById_thenRetrieveIt() {
        var id = userService.addUser(UserDtoFactory.createUserCreationData());

        var userInfo = userService.getUser(id);

        assertThat(userInfo).returns(id, from(UserInfo::id))
                .returns(UserFactory.VALID_USERNAME, from(UserInfo::username));
    }

    @Test
    void givenNotExistingUser_whenGetUserInfo_thenThrow() {
        assertThatThrownBy(() -> userService.getUser(1000)).isInstanceOf(InvalidUserIdError.class);
    }

    @Test
    void givenUser_whenDeleteIt_thenCannotRetrieveItById() {
        var id = userService.addUser(UserDtoFactory.createUserCreationData());

        userService.deleteUser(id);

        assertThatThrownBy(() -> userService.getUser(id)).isInstanceOf(InvalidUserIdError.class);
    }

    @Test
    void givenUserToDelete_whenUserIdDoesNotExist_thenThrowInvalidUserId() {
        assertThatThrownBy(() -> userService.deleteUser(1000)).isInstanceOf(InvalidUserIdError.class);
    }

    @Test
    void givenUserWithShops_whenUserIsDeleted_thenShopsAreDeleted() {
        var shopOwner = UserDataFactory.createUser(this.userRepository);
        var shopCreationDto = ShopDtoFactory.createShopCreation(shopOwner);
        var shopId1 = shopService.addShop(shopCreationDto);
        var shopId2 = shopService.addShop(shopCreationDto);

        this.userService.deleteUser(shopOwner.getId());

        assertThatThrownBy(() -> shopService.findById(shopId1)).isInstanceOf(InvalidShopIdError.class);
        assertThatThrownBy(() -> shopService.findById(shopId2)).isInstanceOf(InvalidShopIdError.class);
    }
}
