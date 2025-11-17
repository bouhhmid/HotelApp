// tn/esprit/gestionhotilere/controller/AdminStatsController.java
package tn.esprit.gestionhotilere.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionhotilere.dto.AdminOverviewDTO;
import tn.esprit.gestionhotilere.entity.Hotel;
import tn.esprit.gestionhotilere.entity.Reservation;
import tn.esprit.gestionhotilere.entity.Role;
import tn.esprit.gestionhotilere.service.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")

public class AdminStatsController {

    private final StatsService statsService;
    private final HotelContextService hotelContextService;
    private final UserService userService;
    /**
     * Overview complet.
     *
     * @param hotelId      (optionnel) si SUPERADMIN: choix; si ADMIN: doit appartenir à l’admin, sinon liste d’hôtels
     * @param seriesBy     "reservation" (défaut) | "checkin"
     * @param windowDays   fenêtre (défaut 7)
     * @param round        décimales (défaut 1)²
     * @param tz           timezone ex. "Africa/Tunis" (défaut = système)
     * @param distinct     true (défaut) → repo distinct pour chambres occupées du jour
     * @param includeTypes true (défaut) → inclure “occupancyByType”
     */
    @GetMapping("/overview")
    public Object overview(
            @RequestParam(required = false) Long hotelId,
            @RequestParam(defaultValue = "reservation") String seriesBy,
            @RequestParam(defaultValue = "7") int windowDays,
            @RequestParam(defaultValue = "1") int round,
            @RequestParam(required = false) String tz,
            @RequestParam(defaultValue = "true") boolean distinct,
            @RequestParam(defaultValue = "true") boolean includeTypes
    ) {
        var user = userService.getOrCreateCurrentUser();

        // Construire les options
        boolean byCheckin = "checkin".equalsIgnoreCase(seriesBy);
        var opts = new StatsService.OverviewOptions(
                windowDays, byCheckin, round, tz, distinct, includeTypes
        );

        if (user.getRole() == Role.SUPERADMIN) {
            if (hotelId == null) {
                // liste pour sélection
                return hotelContextService.getHotelsForCurrentUser();
            }
            return statsService.buildAdminOverview(hotelId, opts);
        }

        // ADMIN : limiter à ses hôtels
        List<Hotel> myHotels = hotelContextService.getHotelsForCurrentUser();
        if (hotelId == null) return myHotels;

        boolean owns = myHotels.stream().anyMatch(h -> h.getId().equals(hotelId));
        if (!owns) throw new RuntimeException("Accès refusé à cet hôtel");

        return statsService.buildAdminOverview(hotelId, opts);
    }

    /**
     * KPIs sur période personnalisée (sans revenus pour l’instant)
     */
    @GetMapping("/overview-range")
    public AdminOverviewDTO overviewRange(
            @RequestParam Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date end,
            @RequestParam(defaultValue = "1") int round,
            @RequestParam(required = false) String tz,
            @RequestParam(defaultValue = "true") boolean includeTypes
    ) {
        if (end.before(start)) {
            throw new IllegalArgumentException("end doit être ≥ start");
        }
        var opts = new StatsService.OverviewOptions(
                Math.max(1, (int) ((end.getTime() - start.getTime()) / (1000L * 60 * 60 * 24)) + 1),
                false, // seriesByCheckin n'a pas d'impact ici
                round,
                tz,
                true,  // distinct: non utilisé ici
                includeTypes
        );
        return statsService.buildOverviewInRangeNoRevenue(hotelId, start, end, opts);
    }

    @GetMapping("/arrivals")
    public Object arrivals(
            @RequestParam Long hotelId,
            @RequestParam(defaultValue = "0") int daysFrom,   // 0 = aujourd’hui
            @RequestParam(defaultValue = "0") int daysTo,     // 0 = aujourd’hui, 1 = demain, 7 = semaine
            @RequestParam(required = false) String tz
    ) {
        var user = userService.getOrCreateCurrentUser();

        if (user.getRole() == Role.SUPERADMIN) {
            return statsService.getArrivals(hotelId, daysFrom, daysTo, tz);
        }
        // ADMIN : guard d’accès
        List<Hotel> myHotels = hotelContextService.getHotelsForCurrentUser();
        boolean owns = myHotels.stream().anyMatch(h -> h.getId().equals(hotelId));
        if (!owns) throw new RuntimeException("Accès refusé à cet hôtel");
        return statsService.getArrivals(hotelId, daysFrom, daysTo, tz);
    }

    /* ===================== Départs ===================== */
    @GetMapping("/departures")
    public Object departures(
            @RequestParam Long hotelId,
            @RequestParam(defaultValue = "0") int daysFrom,
            @RequestParam(defaultValue = "0") int daysTo,
            @RequestParam(required = false) String tz
    ) {
        var user = userService.getOrCreateCurrentUser();

        if (user.getRole() == Role.SUPERADMIN) {
            return statsService.getDepartures(hotelId, daysFrom, daysTo, tz);
        }
        List<Hotel> myHotels = hotelContextService.getHotelsForCurrentUser();
        boolean owns = myHotels.stream().anyMatch(h -> h.getId().equals(hotelId));
        if (!owns) throw new RuntimeException("Accès refusé à cet hôtel");
        return statsService.getDepartures(hotelId, daysFrom, daysTo, tz);
    }

    /* ===================== En attente (paginé) ===================== */
    @GetMapping("/pending")
    public Page<Reservation> pending(
            @RequestParam Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var user = userService.getOrCreateCurrentUser();

        if (user.getRole() == Role.SUPERADMIN) {
            return statsService.getPending(hotelId, page, size);
        }
        List<Hotel> myHotels = hotelContextService.getHotelsForCurrentUser();
        boolean owns = myHotels.stream().anyMatch(h -> h.getId().equals(hotelId));
        if (!owns) throw new RuntimeException("Accès refusé à cet hôtel");
        return statsService.getPending(hotelId, page, size);
    }
    // ➕ À AJOUTER dans AdminStatsController

    /**
     * Résumé compact pour le Dashboard (mois courant, sans revenus pour l’instant)
     */
    // AdminStatsController.java
    @GetMapping("/summary")
        public Object summary(
                @RequestParam(required = false) Long hotelId,
                @RequestParam(required = false, name = "tz") String timezone
        ){
            var user = userService.getOrCreateCurrentUser();
            if (user.getRole() == Role.SUPERADMIN) {
                if (hotelId == null) return hotelContextService.getHotelsForCurrentUser();
                return statsService.buildDashboardSummary(hotelId, timezone);
            }
            List<Hotel> myHotels = hotelContextService.getHotelsForCurrentUser();
            if (hotelId == null) {
                if (myHotels.isEmpty()) throw new RuntimeException("Aucun hôtel disponible");
                hotelId = myHotels.get(0).getId();
            } else {
                Long finalHotelId = hotelId;
                boolean owns = myHotels.stream().anyMatch(h -> h.getId().equals(finalHotelId));
                if (!owns) throw new RuntimeException("Accès refusé à cet hôtel");
            }
            return statsService.buildDashboardSummary(hotelId, timezone);
        }

}