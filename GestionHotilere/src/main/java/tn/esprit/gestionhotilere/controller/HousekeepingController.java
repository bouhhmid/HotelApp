// tn/esprit/gestionhotilere/controller/HousekeepingController.java
package tn.esprit.gestionhotilere.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionhotilere.dto.HousekeepingTaskDTO;
import tn.esprit.gestionhotilere.entity.Hotel;
import tn.esprit.gestionhotilere.entity.Role;
import tn.esprit.gestionhotilere.service.HousekeepingService;
import tn.esprit.gestionhotilere.service.HotelContextService;
import tn.esprit.gestionhotilere.service.UserService;

import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/housekeeping")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")

public class HousekeepingController {

    private final HousekeepingService housekeepingService;
    private final UserService userService;
    private final HotelContextService hotelContextService;

    @GetMapping("/today")
    public List<HousekeepingTaskDTO> today(
            @RequestParam Long hotelId,
            @RequestParam(defaultValue = "Africa/Tunis") String tz
    ){
        var user = userService.getOrCreateCurrentUser();
        if (user.getRole() != Role.SUPERADMIN) {
            List<Hotel> myHotels = hotelContextService.getHotelsForCurrentUser();
            boolean owns = myHotels.stream().anyMatch(h -> h.getId().equals(hotelId));
            if (!owns) throw new RuntimeException("Accès refusé à cet hôtel");
        }
        return housekeepingService.tasksForToday(hotelId, ZoneId.of(tz));
    }
}
