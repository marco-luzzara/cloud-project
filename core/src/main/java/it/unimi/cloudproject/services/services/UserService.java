package it.unimi.cloudproject.services.services;

import it.unimi.cloudproject.infrastructure.annotations.WithMeasuredExecutionTime;
import it.unimi.cloudproject.services.dto.UserCreationData;
import it.unimi.cloudproject.services.dto.UserInfo;
import it.unimi.cloudproject.services.errors.InvalidShopIdError;
import it.unimi.cloudproject.services.errors.InvalidUserIdError;
import it.unimi.cloudproject.services.errors.InvalidUsernameError;
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

//    public List<UserInfo> getUsersSubscribedToShop(int shopId) {
//        return this.userRepository.findUsersByShopId(shopId).stream()
//                .map(ud -> new UserInfo(ud.getId(), ud.getUsername()))
//                .toList();
//    }
//
//    public void addShopToFavorite(int userId, int shopId) {
//        var userData = this.userRepository.findById(userId);
//        if (userData.isEmpty())
//            throw new InvalidUsernameError(userId);
//
//        var shopData = this.shopRepository.findById(shopId);
//        if (shopData.isEmpty())
//            throw new InvalidShopIdError(shopId);
//
//        var user = userData.get().toUser(this.shopRepository);
//        var shop = shopData.get().toShop();
//        user.addFavoriteShop(shop);
//
//        var updatedUser = UserData.fromUser(user);
//        this.userRepository.save(updatedUser);
//    }
}
