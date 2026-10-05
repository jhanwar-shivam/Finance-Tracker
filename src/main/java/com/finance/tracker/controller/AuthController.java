package com.finance.tracker.controller;

import com.finance.tracker.dto.LoginRequestDTO;
import com.finance.tracker.dto.LoginAndRegisterResponseDTO;
import com.finance.tracker.dto.RegisterResuestDTO;

import com.finance.tracker.service.AuthService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/register")
    public LoginAndRegisterResponseDTO register(@Valid @RequestBody RegisterResuestDTO registerResuestDTO) {
        return authService.register(registerResuestDTO);
    }

    @PostMapping("/login")
    public LoginAndRegisterResponseDTO login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        return authService.login(loginRequestDTO);

    }

}
