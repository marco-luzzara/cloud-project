package it.unimi.cloudproject.data.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import org.springframework.data.annotation.Id;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public class User {
    @Id
    @With
    private final Integer id;
    private final String username;

    static User of(String username) {
        return new User(null, username);
    }
}
