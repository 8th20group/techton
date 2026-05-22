package com.techton.auth;

import com.techton.auth.dto.LoginRequest;
import com.techton.auth.dto.LoginResponse;
import com.techton.auth.dto.MeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/auth/me")
    public MeResponse me(@RequestParam String githubId) {
        return authService.me(githubId);
    }
}
