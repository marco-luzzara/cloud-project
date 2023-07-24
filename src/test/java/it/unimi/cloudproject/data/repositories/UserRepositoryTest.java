package it.unimi.cloudproject.data.repositories;

import it.unimi.cloudproject.data.factories.ShopDataFactory;
import it.unimi.cloudproject.data.factories.UserDataFactory;
import it.unimi.cloudproject.data.model.ShopData;
import it.unimi.cloudproject.data.model.UserData;
import it.unimi.cloudproject.data.model.UserShopData;
import it.unimi.cloudproject.factories.bl.UserFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class UserRepositoryTest {

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
    void givenUserRepo_whenAddUser_thenAddItToDb() {
        var user = UserFactory.getUser();
        var userData = UserData.fromUser(user);

        userData = userRepository.save(userData);

        assertThat(userData.getId()).isNotNull();
        assertThat(userRepository.findById(userData.getId()).map(UserData::getUsername)).hasValue(userData.getUsername());
    }

    @Test
    void givenUserRepo_whenRemoveUser_thenRemoveItFromDb() {
        var userData = UserDataFactory.createUser(this.userRepository);
        var userId = userData.getId();

        userRepository.deleteById(userId);

        assertThat(userRepository.findById(userId)).isEmpty();
    }

    @Test
    void givenZeroUsersForShop_whenGetUsersOfAShop_thenReturnsZero() {
        var shopData = ShopDataFactory.createShop(this.shopRepository);

        var users = userRepository.findUsersByShopId(shopData.getId());

        assertThat(users).isEmpty();
    }

    @Test
    void givenManyUsersForShop_whenGetUsersOfAShop_thenReturnsAll() {
        var shopData = ShopDataFactory.createShop(this.shopRepository);
        var userData1 = UserDataFactory.createUser(this.userRepository);
        var userData2 = UserDataFactory.createUser(this.userRepository);
        userData1.getFavoriteShops().add(
                new UserShopData(AggregateReference.to(userData1.getId()), AggregateReference.to(shopData.getId())));
        userData2.getFavoriteShops().add(
                new UserShopData(AggregateReference.to(userData2.getId()), AggregateReference.to(shopData.getId())));
        userRepository.saveAll(List.of(userData1, userData2));

        var foundUsers = userRepository.findUsersByShopId(shopData.getId());

        assertThat(foundUsers).hasSize(2);
        assertThat(foundUsers).extracting(UserData::getId).contains(userData1.getId(), userData2.getId());
    }
}
