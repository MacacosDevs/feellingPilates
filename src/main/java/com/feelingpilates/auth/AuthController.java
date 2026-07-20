package com.feelingpilates.auth;

import com.feelingpilates.auth.dto.GoogleTokenRequest;
import com.feelingpilates.auth.dto.LoginRequest;
import com.feelingpilates.auth.dto.RegistroRequest;
import com.feelingpilates.auth.dto.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public ResponseEntity<TokenResponse> registro(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Stub de login con Google. Cuando se implemente: validar el idToken contra
     * los certificados de Google, crear el usuario con proveedor_auth='google'
     * (contrasena_hash null) si no existe, y devolver un JWT propio.
     */
    @PostMapping("/google")
    public ResponseEntity<Void> google(@Valid @RequestBody GoogleTokenRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
