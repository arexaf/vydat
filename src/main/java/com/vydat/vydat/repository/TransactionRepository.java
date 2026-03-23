package com.vydat.vydat.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.vydat.vydat.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByMerchantRef(String merchantRef);
    Page<Transaction> findByUserId(Long userId, Pageable pageable);
    long countByUserId(Long userId);
}