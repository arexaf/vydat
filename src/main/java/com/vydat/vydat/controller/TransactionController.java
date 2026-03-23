package com.vydat.vydat.controller;

import com.vydat.vydat.service.VtuService;
import com.vydat.vydat.model.Transaction;
import com.vydat.vydat.service.dto.TransactionDTOs.AirtimeRequest;
import com.vydat.vydat.service.dto.TransactionDTOs.CableRequest;
import com.vydat.vydat.service.dto.TransactionDTOs.DataRequest;
import com.vydat.vydat.service.dto.TransactionDTOs.ElectricityRequest;
import com.vydat.vydat.service.dto.TransactionDTOs.ExamRequest;
import com.vydat.vydat.service.dto.TransactionDTOs.ResponseDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final VtuService vtuService;

    // Airtime
    @PostMapping("/airtime")
    public ResponseDTO buyAirtime(@RequestBody AirtimeRequest request) {
        return vtuService.buyAirtime(request);
    }

    // Data
    @PostMapping("/data")
    public ResponseDTO buyData(@RequestBody DataRequest request) {
        return vtuService.buyData(request);
    }

    // Cable TV
    @PostMapping("/cable")
    public ResponseDTO buyCable(@RequestBody CableRequest request) {
        return vtuService.buyCable(request);
    }

    // Electricity
    @PostMapping("/electricity")
    public ResponseDTO buyElectricity(@RequestBody ElectricityRequest request) {
        return vtuService.buyElectricity(request);
    }

    // Exam PINs
    @PostMapping("/exam")
    public ResponseDTO buyExamPin(@RequestBody ExamRequest request) {
        return vtuService.buyExamPin(request);
    }

    @GetMapping("/user/{userId}")
    public Page<Transaction> getUserTransactions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return vtuService.getUserTransactions(userId, PageRequest.of(page, size));
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> getUserTransactionCount(@PathVariable Long userId) {
        long count = vtuService.getUserTransactionCount(userId);
        return ResponseEntity.ok(count);
    }

}