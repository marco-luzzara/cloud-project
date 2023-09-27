package it.unimi.cloudproject.data.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("USER_SHOP")
@AllArgsConstructor
@Getter
public class UserShopData {
    @Column("user_id")
    private final AggregateReference<UserData, Integer> userId;
    @Column("shop_id")
    private final AggregateReference<ShopData, Integer> shopId;
}
