package it.unimi.cloudproject.data.model;

import it.unimi.cloudproject.bl.Shop;
import it.unimi.cloudproject.bl.valueobjects.Coordinates;
import it.unimi.cloudproject.infrastructure.annotations.codecoverage.Generated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Table("SHOP")
public class ShopData {
    @Id
    @With
    private final Integer id;
    private final String name;
    private final AggregateReference<UserData, Integer> shopOwner;
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private final Coordinates coordinates;

    static ShopData of(String name, UserData shopOwner, Coordinates coordinates) {
        return new ShopData(null, name, AggregateReference.to(shopOwner.getId()), coordinates);
    }

    public static ShopData fromShop(Shop shop) {
        return new ShopData(shop.id(), shop.name(), AggregateReference.to(shop.shopOwner().id()), shop.coordinates());
    }

    @Generated
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShopData shopData = (ShopData) o;
        return Objects.equals(name, shopData.name) && Objects.equals(shopOwner.getId(), shopData.shopOwner.getId()) && Objects.equals(coordinates, shopData.coordinates);
    }

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(name, shopOwner.getId(), coordinates);
    }
}
