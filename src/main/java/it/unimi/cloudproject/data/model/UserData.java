package it.unimi.cloudproject.data.model;

import it.unimi.cloudproject.bl.Shop;
import it.unimi.cloudproject.bl.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Table("user")
public class UserData {
    @Id
    @With
    private final Integer id;
    private final String username;
    private final Set<AggregateReference<ShopData, Integer>> favoriteShops;

    public static UserData of(String username, Set<AggregateReference<ShopData, Integer>> favoriteShops) {
        return new UserData(null, username, favoriteShops);
    }

    public static UserData fromUser(User user) {
        return new UserData(user.id(), user.username(), user.favoriteShops().stream()
                .map(s -> AggregateReference.<ShopData, Integer>to(s.id()))
                .collect(Collectors.toSet()));
    }
}
