package com.feelingpilates.salud;

import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/publico")
public class PingController {

    @GetMapping("/ping")
    public PingResponse ping() {
        return new PingResponse("ok", Instant.now());
    }

    public record PingResponse(String status, Instant timestamp) {
    }
}
