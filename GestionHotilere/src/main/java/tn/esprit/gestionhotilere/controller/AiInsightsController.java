package tn.esprit.gestionhotilere.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionhotilere.dto.AiInsightsDTO;
import tn.esprit.gestionhotilere.entity.Hotel;
import tn.esprit.gestionhotilere.entity.Role;
import tn.esprit.gestionhotilere.service.*;

import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/admin/insights")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AiInsightsController {

    private final AiInsightsService ai;
    private final UserService userService;
    private final HotelContextService hotelContextService;

    @GetMapping
    public AiInsightsDTO get(
            @RequestParam Long hotelId,
            @RequestParam(defaultValue = "7") int windowDays,
            @RequestParam(defaultValue = "Africa/Tunis") String tz
    ){
        var user = userService.getOrCreateCurrentUser();
        if (user.getRole() != Role.SUPERADMIN) {
            List<Hotel> myHotels = hotelContextService.getHotelsForCurrentUser();
            boolean owns = myHotels.stream().anyMatch(h -> h.getId().equals(hotelId));
            if (!owns) throw new RuntimeException("Accès refusé à cet hôtel");
        }
        return ai.buildInsights(hotelId, windowDays, ZoneId.of(tz));
    }
}
