package com.deluxeterno.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** El autollenado es una ayuda de presentación por sesión, nunca una autorización. */
@ControllerAdvice
public class DemoViewAdvice {
    public static final String SESSION_KEY = "deluxeDemoEnabled";
    private final boolean available;
    private final boolean isolated;

    public DemoViewAdvice(@Value("${app.demo.available:true}") boolean available, @Value("${app.demo.isolated:false}") boolean isolated) { this.available = available; this.isolated = isolated; }

    @ModelAttribute("demoEnvironment")
    public boolean isolated() { return isolated; }

    @ModelAttribute("demoAvailable")
    public boolean available() { return available; }

    @ModelAttribute("demoEnabled")
    public boolean enabled(HttpServletRequest request) {
        var session = request.getSession(false);
        return available && request.isUserInRole("ADMIN") && session != null
                && Boolean.TRUE.equals(session.getAttribute(SESSION_KEY));
    }
}
