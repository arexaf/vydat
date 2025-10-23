package com.vydat.vydat.controller;

import com.vydat.vydat.model.User;
import com.vydat.vydat.service.UserService;
import com.vydat.vydat.service.WalletService;
import com.vydat.vydat.service.VtuNgService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/paystack")
public class PaystackWebhookController {

    private final UserService userService;
    private final WalletService walletService;
    private final VtuNgService vtuNgService;

    public PaystackWebhookController(UserService userService,
                                      WalletService walletService,
                                      VtuNgService vtuNgService) {
        this.userService = userService;
        this.walletService = walletService;
        this.vtuNgService = vtuNgService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload,
                                                @RequestHeader("x-paystack-signature") String signature) {
        // ⚠️ TODO: Verify signature with your Paystack secret key

        try {
            String event = (String) payload.get("event");

            if ("charge.success".equals(event)) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");

                // Paystack sends amount in Kobo → convert to Naira
                Integer amountKobo = (Integer) data.get("amount");
                double amountNaira = amountKobo / 100.0;

                // Account number that received the payment (dedicated account)
                Map<String, Object> authorization = (Map<String, Object>) data.get("authorization");
                String accountNumber = (String) authorization.get("receiver_bank_account_number");

                // ✅ Find user by virtual account
                User user = userService.findByVirtualAccount(accountNumber);
                if (user != null) {
                    // First: Fund VTU NG main wallet
                    try {
                        vtuNgService.fundVtuWallet(amountNaira);

                        // Only if funding succeeds: credit user's wallet
                        walletService.creditWallet(user.getId(), amountNaira);
                    } catch (Exception e) {
                        // Funding failed → do NOT credit user wallet
                        return ResponseEntity.status(500)
                                .body("Failed to credit VTU NG wallet: " + e.getMessage());
                    }
                }
            }

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
