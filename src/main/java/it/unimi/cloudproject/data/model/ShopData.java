package it.unimi.cloudproject.data.model;

import it.unimi.cloudproject.bl.valueobjects.Coordinates;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Table("shop")
public class ShopData {
    @Id
    @With
    private final Integer id;
    private final String name;
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private final Coordinates coordinates;

    private final Set<AggregateReference<UserData, Integer>> subscribedUsers;

    static ShopData of(String name, Coordinates coordinates, Set<AggregateReference<UserData, Integer>> subscribedUsers) {
        return new ShopData(null, name, coordinates, subscribedUsers);
    }
}
