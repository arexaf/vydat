package com.vydat.vydat.controller;

import com.vydat.vydat.model.User;
import com.vydat.vydat.service.UserService;
import com.vydat.vydat.service.WalletService;
import com.vydat.vydat.service.VtuNgService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/paystack")
public class PaystackWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PaystackWebhookController.class);

    private final UserService userService;
    private final WalletService walletService;
    private final VtuNgService vtuNgService;

    @Value("${paystack.secret.key}")
    private String paystackSecret;

    public PaystackWebhookController(UserService userService,
                                     WalletService walletService,
                                     VtuNgService vtuNgService) {
        this.userService = userService;
        this.walletService = walletService;
        this.vtuNgService = vtuNgService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader("x-paystack-signature") String signature) {

        // Verify signature
        if (!verifySignature(rawBody, signature)) {
            log.warn("Invalid Paystack webhook signature");
            return ResponseEntity.status(401).body("Invalid signature");
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> payload = mapper.readValue(rawBody, Map.class);

            String event = (String) payload.get("event");
            log.info("Paystack webhook received: {}", event);

            if ("charge.success".equals(event)) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");

                // Amount in kobo → naira
                Integer amountKobo = (Integer) data.get("amount");
                double amountNaira = amountKobo / 100.0;
                log.info("Payment received: ₦{}", amountNaira);

                // ✅ Account number is inside authorization.receiver_bank_account_number
                Map<String, Object> authorization = (Map<String, Object>) data.get("authorization");
                String accountNumber = (String) authorization.get("receiver_bank_account_number");
                log.info("Receiver account number: {}", accountNumber);

                if (accountNumber == null) {
                    log.warn("Could not extract account number from webhook payload");
                    return ResponseEntity.ok("Webhook received but account number not found");
                }

                // Find user by virtual account
                User user = userService.findByVirtualAccount(accountNumber);
                log.info("Found user: {} for account: {}", user.getId(), accountNumber);

            // Fund VTU NG wallet — non-blocking
            try {
                vtuNgService.fundVtuWallet(amountNaira);
                log.info("VTU NG wallet funded: ₦{}", amountNaira);
            } catch (Exception e) {
                log.warn("VTU NG funding failed (non-critical): {}", e.getMessage());
                // ✅ Don't return — always credit user wallet
            }

            // Always credit user wallet regardless of VTU NG result
            walletService.addFunds(user.getId(), BigDecimal.valueOf(amountNaira));
            log.info("User wallet credited ₦{} for userId: {}", amountNaira, user.getId());
                        }

            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            log.error("Webhook processing error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    private boolean verifySignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(
                    paystackSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKey);
            byte[] hmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hmac) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().equals(signature);
        } catch (Exception e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }
}