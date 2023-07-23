package it.unimi.cloudproject.data.repositories;

import it.unimi.cloudproject.data.model.UserData;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserData, Integer> {
}
