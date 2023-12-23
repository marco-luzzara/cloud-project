package it.unimi.cloudproject.bl;

import it.unimi.cloudproject.bl.errors.ValidationError;
import it.unimi.cloudproject.infrastructure.annotations.codecoverage.Generated;

import java.util.Objects;

public record User(Integer id, String username) {
    public User {
        if (username == null || username.isEmpty())
            throw new ValidationError.EmptyNameForUserError();
    }

    @Generated
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
