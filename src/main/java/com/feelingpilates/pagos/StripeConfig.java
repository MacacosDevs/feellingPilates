package com.feelingpilates.pagos;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Fija la api key global del SDK de Stripe al arrancar. Mientras
 * STRIPE_SECRET_KEY esté vacío, PagoService falla de forma controlada en vez
 * de llamar a Stripe sin clave (mismo criterio que GoogleTokenVerifier).
 */
@Component
public class StripeConfig {

    private static final Logger log = LoggerFactory.getLogger(StripeConfig.class);

    private final String secretKey;

    public StripeConfig(@Value("${app.stripe.secret-key}") String secretKey) {
        this.secretKey = secretKey;
    }

    @PostConstruct
    public void inicializar() {
        Stripe.apiKey = secretKey;
        log.info("StripeConfig inicializado, app.stripe.secret-key blank={}", secretKey.isBlank());
    }
}
