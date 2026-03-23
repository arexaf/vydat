package com.vydat.vydat.service;

import com.vydat.vydat.model.User;
import com.vydat.vydat.repository.UserRepository;
import com.vydat.vydat.security.JwtUtil;
import com.vydat.vydat.service.dto.LoginResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

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
    public User registerUser(String username, String email, String password, String phone) {
        log.info("Attempting to register user: {}", email);

        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed - email already in use: {}", email);
            throw new RuntimeException("Email already in use");
        }

        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(username, email, hashedPassword);
        user.setPhone(phone);
        User savedUser = userRepository.save(user);
        log.info("User saved to DB with ID: {}", savedUser.getId());

        walletService.createWallet(savedUser.getId());
        log.info("Wallet created for user ID: {}", savedUser.getId());

        try {
            String accountNumber = paystackService.createCustomerAndVirtualAccount(email, username, phone);
            savedUser.setVirtualAccount(accountNumber);
            userRepository.save(savedUser);
            log.info("Paystack virtual account assigned to: {}", email);
        } catch (Exception e) {
            log.warn("Could not assign Paystack account for {}: {}", email, e.getMessage());
        }

        return savedUser;
    }

    // LOGIN
    public LoginResponse loginUser(String email, String password) {
        log.info("Login attempt for email: {}", email);

        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("Login failed - no user found for email: {}", email);
            return new RuntimeException("Invalid email or password");
        });

        log.info("User found: {}, checking password...", email);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Login failed - incorrect password for email: {}", email);
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        log.info("Login successful, token generated for: {}", email);
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

        if (username != null && !username.isEmpty()) user.setUsername(username);
        if (email != null && !email.isEmpty()) user.setEmail(email);
        if (password != null && !password.isEmpty()) user.setPassword(passwordEncoder.encode(password));

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