package com.vydat.vydat.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vydat.vydat.model.Transaction;
import com.vydat.vydat.model.Wallet;
import com.vydat.vydat.repository.TransactionRepository;
import com.vydat.vydat.repository.WalletRepository;
import com.vydat.vydat.service.dto.TransactionDTOs.*;
import com.vydat.vydat.service.dto.TransactionStatus;
import com.vydat.vydat.service.dto.TransactionType;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VtuService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final VtuClient vtuClient;

    private void checkBalance(Wallet wallet, double amount) {
        if (wallet.getBalance().compareTo(BigDecimal.valueOf(amount)) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
    }

    private Transaction initTransaction(Long userId, TransactionType type, double amount) {
        String ref = "vydat-" + UUID.randomUUID();
        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setType(type);
        tx.setMerchantRef(ref);
        tx.setAmountRequested(amount);
        tx.setStatus(TransactionStatus.PENDING);
        return transactionRepository.save(tx);
    }

    private ResponseDTO handleResponse(Transaction tx, Wallet wallet, Map<String, Object> vtuResp, double amount) {
        tx.setVtuResponse(vtuResp.toString());
        tx.setVtuTxnId((String) vtuResp.get("trx_id"));
        tx.setAmountCharged(amount);

        String status = (String) vtuResp.get("status");
        if ("success".equalsIgnoreCase(status)) {
            if (wallet.getBalance().compareTo(BigDecimal.valueOf(amount)) >= 0) {
                wallet.setBalance(wallet.getBalance().subtract(BigDecimal.valueOf(amount)));
                walletRepository.save(wallet);
                tx.setStatus(TransactionStatus.SUCCESS);
            } else {
                tx.setStatus(TransactionStatus.RECONCILE_REQUIRED);
                tx.setNotes("VTU succeeded but wallet insufficient at debit time");
            }
        } else {
            tx.setStatus(TransactionStatus.FAILED);
        }

        transactionRepository.save(tx);
        return new ResponseDTO(
                tx.getStatus() == TransactionStatus.SUCCESS,
                "Transaction " + tx.getStatus(),
                tx
        );
    }

    // -------- Airtime ----------
    @Transactional
    public ResponseDTO buyAirtime(AirtimeRequest request) {
        Wallet wallet = walletRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        checkBalance(wallet, request.getAmount());

        Transaction tx = initTransaction(request.getUserId(), TransactionType.AIRTIME, request.getAmount());
        Map<String, Object> resp = vtuClient.buyAirtime(
                request.getNetwork(),
                request.getPhone(),
                request.getAmount(),
                tx.getMerchantRef()
        );
        return handleResponse(tx, wallet, resp, request.getAmount());
    }

    // -------- Data ----------
    @Transactional
    public ResponseDTO buyData(DataRequest request) {
        Wallet wallet = walletRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        checkBalance(wallet, request.getPlan() != null ? 0 : 0); // you can adjust if plans have fixed price

        Transaction tx = initTransaction(request.getUserId(), TransactionType.DATA, 0);
        Map<String, Object> resp = vtuClient.buyData(
                request.getNetwork(),
                request.getPhone(),
                request.getPlan(),
                tx.getMerchantRef()
        );
        return handleResponse(tx, wallet, resp, 0);
    }

    // -------- Cable ----------
    @Transactional
    public ResponseDTO buyCable(CableRequest request) {
        Wallet wallet = walletRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        checkBalance(wallet, 0); // replace 0 with plan cost

        Transaction tx = initTransaction(request.getUserId(), TransactionType.CABLE, 0);
        Map<String, Object> resp = vtuClient.payCable(
                request.getProvider(),
                request.getSmartCardNumber(),
                request.getPlan(),
                tx.getMerchantRef()
        );
        return handleResponse(tx, wallet, resp, 0);
    }

    // -------- Electricity ----------
    @Transactional
    public ResponseDTO buyElectricity(ElectricityRequest request) {
        Wallet wallet = walletRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        checkBalance(wallet, request.getAmount());

        Transaction tx = initTransaction(request.getUserId(), TransactionType.ELECTRICITY, request.getAmount());
        Map<String, Object> resp = vtuClient.payElectricity(
                request.getDisco(),
                request.getMeterNumber(),
                request.getMeterType(),
                request.getAmount(),
                tx.getMerchantRef()
        );
        return handleResponse(tx, wallet, resp, request.getAmount());
    }

    // -------- Exam ----------
    @Transactional
    public ResponseDTO buyExamPin(ExamRequest request) {
        Wallet wallet = walletRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        double amount = request.getQuantity() * 1000; // assume each PIN is ₦1000, adjust as needed
        checkBalance(wallet, amount);

        Transaction tx = initTransaction(request.getUserId(), TransactionType.EXAM, amount);
        Map<String, Object> resp = vtuClient.buyExamPin(
                request.getExamType(),
                request.getQuantity(),
                tx.getMerchantRef()
        );
        return handleResponse(tx, wallet, resp, amount);
    }
}
