package it.unimi.cloudproject.data.repositories;

import it.unimi.cloudproject.data.model.UserData;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<UserData, Integer> {
    @Query("""
            SELECT u.*
            FROM "USER_SHOP" us
            JOIN "USER" u on us.user_id = u.id
            WHERE us.shop_id = :shopId
            """)
    List<UserData> findUsersByShopId(@Param("shopId") int shopId);

//    Optional<UserData> findByUsername(String username);
}
