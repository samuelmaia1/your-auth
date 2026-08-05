package com.samuelmaia1_github.yourauth.presentation.controller;

import com.samuelmaia1_github.yourauth.domain.account.Account;
import com.samuelmaia1_github.yourauth.domain.account.AccountService;
import com.samuelmaia1_github.yourauth.presentation.dto.account.CreateAccountDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.account.AccountResponseDTO;
import com.samuelmaia1_github.yourauth.presentation.mapper.AccountPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService service;

    @PostMapping("/create")
    public ResponseEntity<AccountResponseDTO> create(@Valid @RequestBody CreateAccountDTO dto) {
        Account account = AccountPresentationMapper.toDomain(dto);
        Account createdAccount = service.create(account);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AccountPresentationMapper.toResponseDTO(createdAccount));
    }
}
