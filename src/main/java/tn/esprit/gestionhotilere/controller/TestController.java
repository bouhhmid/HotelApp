package tn.esprit.gestionhotilere.controller;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/public")
    public String publicAccess() {
        return "pas besoin de token";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Tu es connecté en tant qu'ADMIN";
    }

    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public String clientAccess() {
        return "Tu es connecté en tant que CLIENT";
    }

    @GetMapping("/superadmin")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public String superAdminAccess() {
        return "Tu es connecté en tant que SUPERADMIN";
    }
}

