package it.unimi.cloudproject.data.repositories;

import it.unimi.cloudproject.data.model.ShopData;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShopRepository extends CrudRepository<ShopData, Integer> {
    @Query("""
            SELECT s.*
            FROM USER_SHOP us
            JOIN SHOP s on us.SHOP_ID = s.ID
            WHERE us.USER_ID = :userId
            """)
    List<ShopData> findFavoriteShopsByUserId(@Param("userId") int userId);
}
