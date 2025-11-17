package tn.esprit.gestionhotilere.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionhotilere.entity.Hotel;
import tn.esprit.gestionhotilere.entity.Role;
import tn.esprit.gestionhotilere.service.HotelContextService;
import tn.esprit.gestionhotilere.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")

public class AdminDashboardController {

    private final HotelContextService hotelContextService;
    private final UserService userService;

    @GetMapping
    public Object getDashboard(@RequestParam(required = false) Long hotelId) {
        var user = userService.getOrCreateCurrentUser();

        if (user.getRole() == Role.SUPERADMIN) {
            if (hotelId != null) {
                // ⚠️ Ici on fera plus tard l'appel vers StatsService
                return "Stats pour l'hôtel " + hotelId;
            }
            return hotelContextService.getHotelsForCurrentUser();
        }

        List<Hotel> myHotels = hotelContextService.getHotelsForCurrentUser();

        if (hotelId != null) {
            boolean owns = myHotels.stream().anyMatch(h -> h.getId().equals(hotelId));
            if (!owns) {
                throw new RuntimeException("Accès refusé à cet hôtel");
            }
            // ⚠️ Ici on fera plus tard l'appel vers StatsService
            return "Stats pour l'hôtel " + hotelId;
        }

        return myHotels;
    }
}
