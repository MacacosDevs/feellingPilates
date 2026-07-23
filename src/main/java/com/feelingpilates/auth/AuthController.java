package com.feelingpilates.auth;

import com.feelingpilates.auth.dto.CompletarInvitacionRequest;
import com.feelingpilates.auth.dto.GoogleTokenRequest;
import com.feelingpilates.auth.dto.InvitacionInfoResponse;
import com.feelingpilates.auth.dto.LoginRequest;
import com.feelingpilates.auth.dto.RegistroRequest;
import com.feelingpilates.auth.dto.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse registrar(@Valid @RequestBody RegistroRequest request) {
        return authService.registrar(request);
    }

    @GetMapping("/invitaciones/{token}")
    public InvitacionInfoResponse obtenerInvitacion(@PathVariable String token) {
        return authService.obtenerInvitacion(token);
    }

    @PostMapping("/invitaciones/completar")
    public TokenResponse completarInvitacion(@Valid @RequestBody CompletarInvitacionRequest request) {
        return authService.completarInvitacion(request);
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
