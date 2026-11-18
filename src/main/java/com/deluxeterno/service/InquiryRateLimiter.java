package com.deluxeterno.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Límite local para el formulario público. El proxy inverso, si existe, debe
 * preservar correctamente la dirección remota en vez de aceptar cabeceras del cliente.
 */
@Service
public class InquiryRateLimiter {
    private record Window(Instant startedAt, int attempts) {
    }

    private final ConcurrentHashMap<String, Window> attemptsByAddress = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final Duration window;

    public InquiryRateLimiter(
            @Value("${app.inquiries.rate-limit.max-attempts:5}") int maxAttempts,
            @Value("${app.inquiries.rate-limit.window:10m}") Duration window) {
        this.maxAttempts = maxAttempts;
        this.window = window;
    }

    public boolean tryAcquire(String address) {
        Instant now = Instant.now();
        Instant expiresAt = now.minus(window);
        attemptsByAddress.entrySet().removeIf(entry -> entry.getValue().startedAt().isBefore(expiresAt));

        AtomicBoolean allowed = new AtomicBoolean();
        attemptsByAddress.compute(address, (key, current) -> {
            if (current == null || current.startedAt().isBefore(expiresAt)) {
                allowed.set(true);
                return new Window(now, 1);
            }
            if (current.attempts() >= maxAttempts) {
                allowed.set(false);
                return current;
            }
            allowed.set(true);
            return new Window(current.startedAt(), current.attempts() + 1);
        });
        return allowed.get();
    }
}
