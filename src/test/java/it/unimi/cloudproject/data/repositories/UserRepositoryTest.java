package it.unimi.cloudproject.data.repositories;

import it.unimi.cloudproject.bl.User;
import it.unimi.cloudproject.data.model.UserData;
import it.unimi.cloudproject.factories.bl.UserFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
//@TestPropertySource()
@ActiveProfiles("test")
//@Testcontainers
public class UserRepositoryTest {
//    @Container
//    private static final PostgreSQLContainer postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
//            .withExposedPorts(5432);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DataSource dataSource;

    @Test
    void givenUserRepo_whenAddUser_thenAddItToDb() {
        var user = UserFactory.getUser();
        var userData = UserData.fromUser(user);

        userData = userRepository.save(userData);

        assertThat(userData.getId()).isNotNull();
        assertThat(userRepository.findById(userData.getId()).map(UserData::getUsername)).hasValue(userData.getUsername());
    }
}
