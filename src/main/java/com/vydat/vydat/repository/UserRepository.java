package com.vydat.vydat.repository;

import com.vydat.vydat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);  // ✅ add this
    boolean existsByEmail(String email);       // ✅ already needed in registerUser
    Optional<User> findByVirtualAccount(String virtualAccount); // ✅ needed for Paystack DVA
}
