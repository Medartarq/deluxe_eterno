package com.deluxeterno.web.admin;

import com.deluxeterno.web.DemoViewAdvice;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/admin/demo")
public class DemoController {
    private final boolean available;
    public DemoController(@Value("${app.demo.available:true}") boolean available) { this.available = available; }

    @GetMapping
    public String guide() { checkAvailable(); return "admin/demo"; }

    @PostMapping("/toggle")
    public String toggle(@RequestParam boolean enabled, HttpSession session) {
        checkAvailable();
        if (enabled) session.setAttribute(DemoViewAdvice.SESSION_KEY, true);
        else session.removeAttribute(DemoViewAdvice.SESSION_KEY);
        return "redirect:/admin/demo";
    }

    private void checkAvailable() {
        if (!available) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
