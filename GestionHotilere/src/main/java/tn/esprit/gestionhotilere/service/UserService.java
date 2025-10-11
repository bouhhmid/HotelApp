package tn.esprit.gestionhotilere.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import tn.esprit.gestionhotilere.entity.Role;
import tn.esprit.gestionhotilere.entity.User;
import tn.esprit.gestionhotilere.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private Jwt getJwt() {
        return (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public User getOrCreateCurrentUser() {
        Jwt jwt = getJwt();
        String sub = jwt.getSubject();
        Optional<User> existingOpt = userRepository.findByidk(sub);

        Role newRole = Role.CLIENT; // valeur par défaut

        try {
            Object claim = jwt.getClaims().get("realm_access");
            if (claim instanceof Map<?, ?> realmAccess) {
                Object rolesObj = realmAccess.get("roles");

                if (rolesObj instanceof List<?> rolesList) {
                    List<String> roles = rolesList.stream()
                            .filter(String.class::isInstance)
                            .map(String.class::cast)
                            .toList();

                    if (roles.contains("ROLE_SUPERADMIN")) {
                        newRole = Role.SUPERADMIN;
                    } else if (roles.contains("ROLE_ADMIN")) {
                        newRole = Role.ADMIN;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Erreur lors de l'extraction des rôles du token : " + e.getMessage());
        }

        if (existingOpt.isPresent()) {
            User existing = existingOpt.get();

            if (!existing.getRole().equals(newRole)) {
                existing.setRole(newRole);
                return userRepository.save(existing);
            }

            return existing;
        }

        // Création si nouveau
        User newUser = new User();
        newUser.setIdk(sub);
        newUser.setEmail(jwt.getClaim("email"));
        newUser.setNom(jwt.getClaim("family_name"));
        newUser.setPrenom(jwt.getClaim("given_name"));
        newUser.setRole(newRole);

        return userRepository.save(newUser);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getByIdkc(String idkc) {
        return userRepository.findByidk(idkc)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

}
