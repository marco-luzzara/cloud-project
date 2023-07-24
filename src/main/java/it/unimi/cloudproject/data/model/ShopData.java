package it.unimi.cloudproject.data.model;

import it.unimi.cloudproject.bl.Shop;
import it.unimi.cloudproject.bl.valueobjects.Coordinates;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Table("SHOP")
public class ShopData {
    @Id
    @With
    private final Integer id;
    private final String name;
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private final Coordinates coordinates;
//    @MappedCollection(idColumn = "SHOP_ID")
//    private Set<UserShopData> subscribedUsers;

    static ShopData of(String name, Coordinates coordinates) {
        return new ShopData(null, name, coordinates);
    }

    public static ShopData fromShop(Shop shop) {
        return new ShopData(shop.id(), shop.name(), shop.coordinates());
    }

    public Shop toShop() {
        return new Shop(this.getId(), this.getName(), this.getCoordinates());
    }
}
