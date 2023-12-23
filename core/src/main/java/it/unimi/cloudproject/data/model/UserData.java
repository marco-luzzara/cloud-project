package it.unimi.cloudproject.data.model;

import it.unimi.cloudproject.bl.User;
import it.unimi.cloudproject.infrastructure.annotations.codecoverage.Generated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Table("USER")
public class UserData {
    @Id
    @With
    private final Integer id;
    private final String username;

    public static UserData of(String username) {
        return new UserData(null, username);
    }

    public User toUser() {
        return new User(this.getId(), this.getUsername());
    }

    public static UserData fromUser(User user) {
        return new UserData(user.id(), user.username());
    }

    @Generated
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserData userData = (UserData) o;
        return Objects.equals(username, userData.username);
    }

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
