package com.feelingpilates.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * Valida el idToken de Google contra los certificados públicos de Google
 * (JWKS), verificando que la audiencia sea nuestro Web Client ID de OAuth.
 */
@Component
public class GoogleTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifier.class);

    public record GooglePerfil(String correo, String nombre, String fotoUrl) {
    }

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(@Value("${app.google.client-id}") String clientId) {
        this.verifier = clientId.isBlank()
                ? null
                : new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                        .setAudience(Collections.singletonList(clientId))
                        .build();
        log.info("GoogleTokenVerifier inicializado, app.google.client-id blank={}", clientId.isBlank());
    }

    public Optional<GooglePerfil> verificar(String idTokenString) {
        if (verifier == null) {
            // GOOGLE_CLIENT_ID no configurado todavía; falla de forma controlada
            // en vez de aceptar tokens sin poder validar su audiencia.
            log.warn("verificar() llamado pero app.google.client-id está vacío/no configurado");
            return Optional.empty();
        }
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                log.warn("Verificación de idToken de Google devolvió null (firma o audiencia no coinciden)");
                return Optional.empty();
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            String correo = payload.getEmail();
            String nombre = (String) payload.get("name");
            String fotoUrl = (String) payload.get("picture");
            return Optional.of(new GooglePerfil(correo, nombre != null ? nombre : correo, fotoUrl));
        } catch (GeneralSecurityException | IOException | IllegalArgumentException e) {
            log.warn("Fallo al verificar idToken de Google: {}", e.toString(), e);
            return Optional.empty();
        }
    }
}
