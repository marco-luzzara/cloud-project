package it.unimi.cloudproject.data.repositories;

import it.unimi.cloudproject.data.model.UserData;
import it.unimi.cloudproject.factories.bl.UserFactory;
import it.unimi.cloudproject.factories.data.UserDataFactory;
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

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
public class UserRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> db = DbFactory.getPostgresContainer();

    @DynamicPropertySource
    static void dbProperties(DynamicPropertyRegistry registry) {
        DynamicPropertiesInjector.injectDatasourceFromPostgresContainer(registry, db);
    }

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
    void givenUserRepo_whenAddUser_thenCanRetrieveIt() {
        var user = UserFactory.getUser();
        var userData = UserData.fromUser(user);

        userData = userRepository.save(userData);

        var retrievedUserData = userRepository.findById(userData.getId()).orElseThrow();
        assertThat(retrievedUserData.getId()).isEqualTo(userData.getId());
        assertThat(retrievedUserData).isEqualTo(userData);
    }

    @Test
    void givenUserRepo_whenRemoveUser_thenCannotBeRetrieved() {
        var userData = UserDataFactory.createUser(this.userRepository);
        var userId = userData.getId();

        userRepository.deleteById(userId);

        assertThat(userRepository.findById(userData.getId())).isEmpty();
    }
}
