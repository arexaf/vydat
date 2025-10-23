package com.vydat.vydat.controller;

import com.vydat.vydat.model.User;
import com.vydat.vydat.service.UserService;
import com.vydat.vydat.service.dto.LoginRequest;
import com.vydat.vydat.service.dto.LoginResponse;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // CREATE (Register User)
    @PostMapping("/register")
    public User registerUser(@RequestParam String username,
                             @RequestParam String email,
                             @RequestParam String password) {
        return userService.registerUser(username, email, password);
    }

    // LOGIN
    @PostMapping("/login")
 public LoginResponse loginUser(@RequestBody LoginRequest request) {
    return userService.loginUser(request.getEmail(), request.getPassword());
}


    // READ (Get User by ID)
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // READ (Get All Users)
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // UPDATE
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id,
                           @RequestParam(required = false) String username,
                           @RequestParam(required = false) String email,
                           @RequestParam(required = false) String password) {
        return userService.updateUser(id, username, email, password);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "User with ID " + id + " deleted successfully.";
    }
}
