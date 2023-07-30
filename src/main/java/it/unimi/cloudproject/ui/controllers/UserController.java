package it.unimi.cloudproject.ui.controllers;

import it.unimi.cloudproject.application.dto.UserCreation;
import it.unimi.cloudproject.application.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/user")
public class UserController {
//    @Autowired
//    private UserService userService;
//
//    @PostMapping("/")
//    int addUser(@RequestBody UserCreation userCreation) {
//        return userService.addUser(userCreation);
//    }
//
//    @GetMapping("/{id}")
//    int getUser(@RequestBody UserCreation userCreation) {
//        return userService.getUser(userCreation);
//    }
}
