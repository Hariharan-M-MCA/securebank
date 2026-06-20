package com.securebank.controller;

import com.securebank.dto.request.CreateAccountRequest;
import com.securebank.dto.response.AccountResponse;
import com.securebank.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor

public class AccountController {
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(accountService.createAccount(request, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getUserAccounts(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(accountService.getUserAccounts(userDetails.getUsername()));
    }

}
