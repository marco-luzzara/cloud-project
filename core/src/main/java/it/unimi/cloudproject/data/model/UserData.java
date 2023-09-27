package it.unimi.cloudproject.data.model;

import it.unimi.cloudproject.bl.User;
import it.unimi.cloudproject.data.repositories.ShopRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Table("USER")
public class UserData {
    @Id
    @With
    private final Integer id;
    private final String username;
    @MappedCollection(idColumn = "user_id")
    private final Set<UserShopData> favoriteShops;

    public static UserData of(String username) {
        return new UserData(null, username, new HashSet<>());
    }

    public User toUser(ShopRepository shopRepository) {
        return new User(this.getId(), this.getUsername(), this.getFavoriteShops().stream()
                .map(us -> shopRepository.findById(Optional.ofNullable(us.getShopId().getId()).orElseThrow(() -> new IllegalStateException("the user %d does have a NULL shop associated".formatted(this.getId()))))
                        .orElseThrow(() -> new IllegalStateException("shop with id %d does not exist".formatted(us.getShopId().getId()))))
                .map(ShopData::toShop)
                .collect(Collectors.toSet())
        );
    }

    public static UserData fromUser(User user) {
        // not saved yet
        if (Objects.isNull(user.id()))
            return UserData.of(user.username());
        else
            return new UserData(user.id(), user.username(), user.favoriteShops().stream()
                    .map(s -> new UserShopData(AggregateReference.to(user.id()),
                            AggregateReference.to(s.id())))
                    .collect(Collectors.toSet()));
    }
}
