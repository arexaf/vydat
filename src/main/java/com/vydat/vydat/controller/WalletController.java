package com.vydat.vydat.controller;

import com.vydat.vydat.model.Wallet;
import com.vydat.vydat.service.WalletService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/{userId}")
    public Wallet createWallet(@PathVariable Long userId) {
        return walletService.createWallet(userId);
    }

    @PostMapping("/{userId}/add")
    public Wallet addFunds(@PathVariable Long userId, @RequestParam BigDecimal amount) {
        return walletService.addFunds(userId, amount);
    }

    @PostMapping("/{userId}/deduct")
    public Wallet deductFunds(@PathVariable Long userId, @RequestParam BigDecimal amount) {
        return walletService.deductFunds(userId, amount);
    }

    @GetMapping("/{userId}")
    public Wallet getWallet(@PathVariable Long userId) {
        return walletService.getWallet(userId);
    }
}
