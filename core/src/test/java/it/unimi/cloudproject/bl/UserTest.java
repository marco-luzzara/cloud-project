package it.unimi.cloudproject.bl;

import it.unimi.cloudproject.bl.errors.ValidationError;
import it.unimi.cloudproject.factories.bl.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

public class UserTest {
    @ParameterizedTest
    @NullAndEmptySource
    void givenUserConstructor_whenParamsNull_thenThrow(String username) {
        Assertions.assertThrows(ValidationError.EmptyNameForUserError.class, () ->
                new User(UserFactory.VALID_ID, username));
    }

    @Test
    void givenUserConstructor_whenParamsValid_thenOk() {
        var user = UserFactory.getUser();

        Assertions.assertEquals(UserFactory.VALID_ID, user.id());
        Assertions.assertEquals(UserFactory.VALID_USERNAME, user.username());
    }
}
