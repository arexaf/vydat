package com.vydat.vydat.service;

import com.vydat.vydat.model.Wallet;
import com.vydat.vydat.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final VtuNgService vtuNgService; // Added

    public WalletService(WalletRepository walletRepository, VtuNgService vtuNgService) {
        this.walletRepository = walletRepository;
        this.vtuNgService = vtuNgService;
    }

    // Create wallet for a new user
    public Wallet createWallet(Long userId) {
        Wallet wallet = new Wallet(userId);
        wallet.setBalance(BigDecimal.ZERO);
        return walletRepository.save(wallet);
    }

    // Add funds manually (e.g., admin top-up or bonuses)
    public Wallet addFunds(Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(amount));
        return walletRepository.save(wallet);
    }

    // Deduct funds when user spends
    public Wallet deductFunds(Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        return walletRepository.save(wallet);
    }

    // Get wallet by user
    public Wallet getWallet(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    // ✅ Credit wallet via webhook (automatic deposit from Paystack) and fund VTU NG account
    public Wallet creditWallet(Long userId, double amountNaira) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(BigDecimal.valueOf(amountNaira)));
        return walletRepository.save(wallet);
    }
}
