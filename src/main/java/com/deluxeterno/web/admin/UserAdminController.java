package com.deluxeterno.web.admin;

import com.deluxeterno.dto.UserForm;
import com.deluxeterno.service.DuplicateUsernameException;
import com.deluxeterno.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class UserAdminController {
    private final UserService users;

    public UserAdminController(UserService users) { this.users = users; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", users.findAll());
        return "admin/users";
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("userForm", new UserForm());
        return "admin/user-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("userForm") UserForm form,
            BindingResult errors, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try { users.create(form); }
            catch (DuplicateUsernameException exception) {
                errors.rejectValue("username", "user.duplicate", exception.getMessage());
            }
        }
        // No conservar contraseñas en el objeto que se devuelve a la vista.
        form.setPassword("");
        form.setPasswordConfirmation("");
        if (errors.hasErrors()) return "admin/user-form";
        redirect.addFlashAttribute("successMessage", "Usuario creado. Ya puede iniciar sesión con su propia cuenta.");
        return "redirect:/admin/users";
    }
}
