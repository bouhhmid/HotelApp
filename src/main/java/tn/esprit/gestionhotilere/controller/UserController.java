package tn.esprit.gestionhotilere.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionhotilere.entity.User;
import tn.esprit.gestionhotilere.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private  UserService userService;


    @GetMapping("/me")
    public User getCurrentUser() {
        return userService.getOrCreateCurrentUser();
    }


    @GetMapping
    @PreAuthorize("hasAnyRole( 'SUPERADMIN')")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }


    @GetMapping("/{idk}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public User getUserByIdk(@PathVariable String idk) {
        return userService.getByIdkc(idk);
    }


}
