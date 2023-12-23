package it.unimi.cloudproject.services.services;

import it.unimi.cloudproject.bl.User;
import it.unimi.cloudproject.data.model.UserData;
import it.unimi.cloudproject.data.repositories.UserRepository;
import it.unimi.cloudproject.services.dto.UserCreationData;
import it.unimi.cloudproject.services.dto.UserInfo;
import it.unimi.cloudproject.services.errors.InvalidUserIdError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public int addUser(UserCreationData userCreationData) {
        var user = new User(null, userCreationData.username());

        var createdUser = this.userRepository.save(UserData.fromUser(user));

        return createdUser.getId();
    }

    public void deleteUser(int userId) {
        if (!this.userRepository.existsById(userId))
            throw new InvalidUserIdError(userId);

        this.userRepository.deleteById(userId);
    }

    public UserInfo getUser(int userId) {
        var optionalUser = this.userRepository.findById(userId);

        return optionalUser.map(user -> new UserInfo(user.getId(), user.getUsername())).orElseThrow(() -> new InvalidUserIdError(userId));
    }
}
