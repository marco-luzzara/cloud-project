package it.unimi.cloudproject.data.repositories;

import it.unimi.cloudproject.data.model.ShopData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopRepository extends CrudRepository<ShopData, Integer> {
    List<ShopData> findByName(String name);
}
