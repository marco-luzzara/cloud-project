package it.unimi.cloudproject.application.services;

import it.unimi.cloudproject.application.dto.UserCreationRequest;
import it.unimi.cloudproject.application.dto.UserInfo;
import it.unimi.cloudproject.application.dto.responses.UserCreationResponse;
import it.unimi.cloudproject.application.errors.InvalidShopIdError;
import it.unimi.cloudproject.application.errors.InvalidUsernameError;
import it.unimi.cloudproject.bl.User;
import it.unimi.cloudproject.data.model.UserData;
import it.unimi.cloudproject.data.repositories.ShopRepository;
import it.unimi.cloudproject.data.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ShopRepository shopRepository;

    public UserCreationResponse addUser(UserCreationRequest userCreationRequest) {
        var user = new User(null, userCreationRequest.username(), new HashSet<>());

        var createdUser = this.userRepository.save(UserData.fromUser(user));

        return new UserCreationResponse(createdUser.getId());
    }

    public void deleteUser(Integer userId) {
        this.userRepository.deleteById(userId);
    }

    public Optional<UserInfo> getUser(String username) {
        var optionalUser = this.userRepository.findByUsername(username);

        return optionalUser.map(user -> new UserInfo(user.getId(), user.getUsername()));
    }

    public List<UserInfo> getUsersSubscribedToShop(int shopId) {
        return this.userRepository.findUsersByShopId(shopId).stream()
                .map(ud -> new UserInfo(ud.getId(), ud.getUsername()))
                .toList();
    }

    public void addShopToFavorite(String username, int shopId) {
        var userData = this.userRepository.findByUsername(username);
        if (userData.isEmpty())
            throw new InvalidUsernameError(username);

        var shopData = this.shopRepository.findById(shopId);
        if (shopData.isEmpty())
            throw new InvalidShopIdError(shopId);

        var user = userData.get().toUser(this.shopRepository);
        var shop = shopData.get().toShop();
        user.addFavoriteShop(shop);

        var updatedUser = UserData.fromUser(user);
        this.userRepository.save(updatedUser);
    }
}
