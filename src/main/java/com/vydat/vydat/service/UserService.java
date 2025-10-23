package com.vydat.vydat.service;

import com.vydat.vydat.model.User;
import com.vydat.vydat.repository.UserRepository;
import com.vydat.vydat.security.JwtUtil;
import com.vydat.vydat.service.dto.LoginResponse;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final PaystackService paystackService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository,
                       WalletService walletService,
                       PaystackService paystackService,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.paystackService = paystackService;
        this.jwtUtil = jwtUtil;
    }

    // CREATE (Register User)
    public User registerUser(String username, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already in use");
        }

        // ✅ hash password before saving
        String hashedPassword = passwordEncoder.encode(password);

        User user = new User(username, email, hashedPassword);
        User savedUser = userRepository.save(user);

        // Automatically create wallet for this user
        walletService.createWallet(savedUser.getId());

        // Create Paystack virtual account
        try {
            String accountNumber = paystackService.createVirtualAccount(email);
            savedUser.setVirtualAccount(accountNumber);
            userRepository.save(savedUser);
        } catch (Exception e) {
            throw new RuntimeException("User created but failed to assign Paystack account: " + e.getMessage());
        }

        return savedUser;
    }

public LoginResponse loginUser(String email, String password) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Invalid email or password"));

    if (!passwordEncoder.matches(password, user.getPassword())) {
        throw new RuntimeException("Invalid email or password");
    }

    String token = jwtUtil.generateToken(user.getEmail());
    return new LoginResponse(token, user);
}

// READ (Get User by ID)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // READ (Get All Users)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // UPDATE
    public User updateUser(Long id, String username, String email, String password) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (username != null && !username.isEmpty()) {
            user.setUsername(username);
        }
        if (email != null && !email.isEmpty()) {
            user.setEmail(email);
        }
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password)); // ✅ rehash password
        }

        return userRepository.save(user);
    }

    public User findByVirtualAccount(String accountNumber) {
        return userRepository.findByVirtualAccount(accountNumber)
                .orElseThrow(() -> new RuntimeException("User not found for account: " + accountNumber));
    }

    // DELETE
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }
}
