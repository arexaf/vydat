package com.vydat.vydat.controller;
import com.vydat.vydat.service.VtuWalletService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vtu")
public class VtuController {
    private final VtuWalletService vtuWalletService;

    public VtuController(VtuWalletService vtuWalletService) {
        this.vtuWalletService = vtuWalletService;

    }

    @GetMapping("/balance")
    public String getBalance(){
        return vtuWalletService.getBalance();
    }
}
