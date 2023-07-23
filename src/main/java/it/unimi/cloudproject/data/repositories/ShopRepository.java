package it.unimi.cloudproject.data.repositories;

import it.unimi.cloudproject.data.model.ShopData;
import org.springframework.data.repository.CrudRepository;

public interface ShopRepository extends CrudRepository<ShopData, Integer> {
}
