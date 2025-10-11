// tn/esprit/gestionhotilere/service/StatsService.java
package tn.esprit.gestionhotilere.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tn.esprit.gestionhotilere.dto.AdminOverviewDTO;
import tn.esprit.gestionhotilere.dto.DashboardSummaryDTO;
import tn.esprit.gestionhotilere.dto.PointXY;
import tn.esprit.gestionhotilere.entity.Reservation;
import tn.esprit.gestionhotilere.entity.Statut;
import tn.esprit.gestionhotilere.repository.ChambreRepository;
import tn.esprit.gestionhotilere.repository.ReservationRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final ReservationRepository reservationRepository;
    private final ChambreRepository chambreRepository;

    /* ----------------------------- Options ----------------------------- */
    /** Options d’affichage/agrégation pour les overviews */
    public record OverviewOptions(
            int windowDays,
            boolean seriesByCheckin,
            int round,
            String timezone,
            boolean distinctOccupiedRooms,
            boolean includeOccupancyByType
    ) {
        public static OverviewOptions defaults() {
            return new OverviewOptions(7, false, 1, null, true, true);
        }
    }

    /* ----------------------------- Helpers temps & maths ----------------------------- */
    private ZoneId z(OverviewOptions opts) {
        try { return (opts != null && opts.timezone != null) ? ZoneId.of(opts.timezone) : ZoneId.systemDefault(); }
        catch (Exception ignored) { return ZoneId.systemDefault(); }
    }
    private Date atStart(LocalDate d, ZoneId z){ return Date.from(d.atStartOfDay(z).toInstant()); }
    private Date today(ZoneId z){ return atStart(LocalDate.now(z), z); }
    private Date daysAgo(int n, ZoneId z){ return atStart(LocalDate.now(z).minusDays(n), z); }
    private Date firstDayThisMonth(ZoneId z){ return atStart(LocalDate.now(z).withDayOfMonth(1), z); }

    private long daysBetween(Date start, Date end){
        long d = (end.getTime() - start.getTime())/(1000L*60*60*24);
        return Math.max(d, 1);
    }
    private long nightsOverlap(Date windowStart, Date windowEnd, Date a, Date b) {
        long s = Math.max(windowStart.getTime(), a.getTime());
        long e = Math.min(windowEnd.getTime(), b.getTime());
        if (e <= s) return 0;
        return (e - s) / (1000L*60*60*24);
    }
    private double r(double v, int dec){ double m = Math.pow(10, Math.max(dec,0)); return Math.round(v*m)/m; }

    /* ----------------------------- Overview “tout-en-un” ----------------------------- */
    public AdminOverviewDTO buildAdminOverview(Long hotelId) {
        // rétro-compat: options par défaut
        return buildAdminOverview(hotelId, OverviewOptions.defaults());
    }

    public AdminOverviewDTO buildAdminOverview(Long hotelId, OverviewOptions opts){
        ZoneId zone = z(opts);
        int window = Math.max(1, opts.windowDays());
        Date dToday = today(zone);
        Date from = daysAgo(window-1, zone);

        // 1) Taux d’occupation du jour (option DISTINCT)
        long totalChambres = chambreRepository.countByHotelId(hotelId);
        long occupees = reservationRepository.countChambresOccupeesJour(hotelId, dToday);
        // si l’implémentation repo n’est pas DISTINCT, on peut “sécuriser” ici,
        // mais comme tu as déjà la version DISTINCT côté repo, on garde tel quel.
        double tauxOcc = totalChambres == 0 ? 0 : (100.0 * occupees / totalChambres);

        // 2) KPIs simples
        long enAttente = reservationRepository.countReservationsEnAttente(hotelId);
        long nbJour = reservationRepository.countReservationsJour(hotelId, dToday);

        // 3) Série window jours : par dateReservation (par défaut) ou par check-in (dateDebut)
        Map<String, Long> series = baseSeriesSkeleton(window, zone);
        if (opts.seriesByCheckin()) {
            // check-ins = group by dateDebut
            countCheckinsLastDaysInService(hotelId, from, dToday, zone)
                    .forEach((k,v) -> series.put(k, v));
        } else {
            // créations = group by dateReservation
            reservationRepository.countReservationsLastDays(hotelId, from).forEach(row -> {
                String d = String.valueOf(row[0]);
                long c = ((Number)row[1]).longValue();
                series.put(d, c);
            });
        }
        List<PointXY> reservations7J = mapToPoints(series);

        // 4) KPIs avancés sur le mois en cours (sans paiements)
        Date monthStart = firstDayThisMonth(zone);
        Date now = new Date();
        var k = computeAdvancedNoRevenue(hotelId, monthStart, now, opts);

        // 5) Arrondis propres pour l’UI
        double tauxOccR = r(tauxOcc, opts.round());
        double avgLOSR = r(k.avgLOS, opts.round());
        double cancelRateR = r(k.cancelRate, opts.round());
        Map<String, Double> occTypeRounded = k.occupancyByType;
        if (occTypeRounded != null) {
            occTypeRounded = occTypeRounded.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> r(e.getValue(), opts.round()),
                            (a,b)->a, LinkedHashMap::new));
        }

        return AdminOverviewDTO.builder()
                .tauxOccupationJour(tauxOccR)
                .nbReservationsEnAttente(enAttente)
                .nbReservationsJour(nbJour)
                .reservations7J(reservations7J)
                .avgLengthOfStay(avgLOSR)
                .cancelRate(cancelRateR)
                .occupancyByType(occTypeRounded)
                .revenuMois(0d)           // pas de paiements pour l’instant
                .revenu12M(List.of())
                .build();
    }

    /* ----------------------------- KPIs avancés (sans revenu) ----------------------------- */
    private record KPI(double avgLOS, double cancelRate, Map<String, Double> occupancyByType) {}

    public KPI computeAdvancedNoRevenue(Long hotelId, Date start, Date end, OverviewOptions opts){
        boolean includeType = opts == null || opts.includeOccupancyByType();
        // 1) Longueur de séjour moyenne + nuitées occupées par type
        List<Reservation> confirmed = reservationRepository.findConfirmedOverlapping(hotelId, start, end);
        long sumStayNights = 0; long stayCount = 0;
        Map<String, Long> occupiedNightsByType = new HashMap<>();

        for (Reservation r : confirmed) {
            long stay = nightsOverlap(r.getDateDebut(), r.getDateFin(), r.getDateDebut(), r.getDateFin());
            if (stay > 0) { sumStayNights += stay; stayCount++; }
            if (includeType && r.getChambre() != null && r.getChambre().getTypeChambre() != null) {
                String type = r.getChambre().getTypeChambre().name();
                long n = nightsOverlap(start, end, r.getDateDebut(), r.getDateFin());
                occupiedNightsByType.merge(type, n, Long::sum);
            }
        }
        double avgLOS = (stayCount == 0) ? 0 : (double) sumStayNights / stayCount;

        // 2) Taux d’annulation (sur la période)
        long cancelled = reservationRepository.countCancelledInRange(hotelId, start, end);
        long totalBooked = reservationRepository.countAllInRange(hotelId, start, end);
        double cancelRate = (totalBooked == 0) ? 0 : (100.0 * cancelled / totalBooked);

        // 3) Occupation par type (optionnel)
        Map<String, Double> occupancyByType = null;
        if (includeType) {
            List<Object[]> counts = chambreRepository.countByType(hotelId);
            long days = daysBetween(start, end);
            occupancyByType = new LinkedHashMap<>();
            for (Object[] row : counts) {
                String type = String.valueOf(row[0]);
                long rooms = ((Number)row[1]).longValue();
                long occNights = occupiedNightsByType.getOrDefault(type, 0L);
                double ratio = (rooms == 0 || days == 0) ? 0 : (100.0 * occNights / (rooms * days));
                occupancyByType.put(type, ratio);
            }
        }

        return new KPI(avgLOS, cancelRate, occupancyByType);
    }

    // rétro-compat (appelé par /overview-range actuel)
    public AdminOverviewDTO buildOverviewInRangeNoRevenue(Long hotelId, Date start, Date end){
        return buildOverviewInRangeNoRevenue(hotelId, start, end, OverviewOptions.defaults());
    }

    public AdminOverviewDTO buildOverviewInRangeNoRevenue(Long hotelId, Date start, Date end, OverviewOptions opts){
        var k = computeAdvancedNoRevenue(hotelId, start, end, opts);
        Map<String, Double> occTypeRounded = k.occupancyByType;
        if (occTypeRounded != null) {
            int dec = opts == null ? 1 : opts.round();
            occTypeRounded = occTypeRounded.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> r(e.getValue(), dec),
                            (a,b)->a, LinkedHashMap::new));
        }
        return AdminOverviewDTO.builder()
                .avgLengthOfStay(r(k.avgLOS, opts.round()))
                .cancelRate(r(k.cancelRate, opts.round()))
                .occupancyByType(occTypeRounded)
                .revenuMois(0d)
                .revenu12M(List.of())
                .build();
    }

    /* ----------------------------- Utilitaires internes ----------------------------- */
    private Map<String, Long> baseSeriesSkeleton(int windowDays, ZoneId zone){
        Map<String, Long> map = new LinkedHashMap<>();
        for (int i = windowDays-1; i >= 0; i--) {
            LocalDate d = LocalDate.now(zone).minusDays(i);
            map.put(d.toString(), 0L);
        }
        return map;
    }
    private List<PointXY> mapToPoints(Map<String, Long> map){
        return map.entrySet().stream()
                .map(e -> PointXY.builder().x(e.getKey()).y(e.getValue()).build())
                .collect(Collectors.toList());
    }

    /**
     * Série des check-ins sur [from, to]. Sans nouveau repo :
     * on réutilise findConfirmedOverlapping et on filtre par dateDebut ∈ [from, to].
     */
    private Map<String, Long> countCheckinsLastDaysInService(Long hotelId, Date from, Date to, ZoneId zone){
        Map<String, Long> res = baseSeriesSkeleton((int)daysBetween(from, to)+1, zone);
        List<Reservation> confirmed = reservationRepository.findConfirmedOverlapping(hotelId, from, to);
        confirmed.stream()
                .filter(r -> r.getDateDebut() != null && !r.getDateDebut().before(from) && !r.getDateDebut().after(to))
                .collect(Collectors.groupingBy(r -> atStart(r.getDateDebut().toInstant().atZone(zone).toLocalDate(), zone).toString(),
                        LinkedHashMap::new, Collectors.counting()))
                .forEach(res::put);
        return res;
    }
    private Date startOfDay(LocalDate d, ZoneId z){ return Date.from(d.atStartOfDay(z).toInstant()); }
    private Date endOfDay(LocalDate d, ZoneId z){ return Date.from(d.plusDays(1).atStartOfDay(z).toInstant()); }
    private Date plusDays(Date base, int days, ZoneId z){
        return startOfDay(base.toInstant().atZone(z).toLocalDate().plusDays(days), z);
    }

    /* ========= Arrivées (check‑ins) dans [daysFrom, daysTo] ========= */
    public List<Reservation> getArrivals(Long hotelId, int daysFrom, int daysTo, String timezone) {
        ZoneId z = (timezone == null || timezone.isBlank()) ? ZoneId.systemDefault() : ZoneId.of(timezone);
        if (daysTo < daysFrom) { int t = daysFrom; daysFrom = daysTo; daysTo = t; }
        LocalDate todayLD = LocalDate.now(z);
        Date start = startOfDay(todayLD.plusDays(daysFrom), z);
        Date end   = endOfDay(todayLD.plusDays(daysTo), z);

        // On réutilise la requête “overlapping” et on garde uniquement dateDebut dans la fenêtre
        return reservationRepository.findConfirmedOverlapping(hotelId, start, end).stream()
                .filter(r -> r.getDateDebut() != null
                        && !r.getDateDebut().before(start)
                        && r.getDateDebut().before(end))
                .sorted(Comparator.comparing(Reservation::getDateDebut))
                .toList();
    }

    /* ========= Départs (check‑outs) dans [daysFrom, daysTo] ========= */
    public List<Reservation> getDepartures(Long hotelId, int daysFrom, int daysTo, String timezone) {
        ZoneId z = (timezone == null || timezone.isBlank()) ? ZoneId.systemDefault() : ZoneId.of(timezone);
        if (daysTo < daysFrom) { int t = daysFrom; daysFrom = daysTo; daysTo = t; }
        LocalDate todayLD = LocalDate.now(z);
        Date start = startOfDay(todayLD.plusDays(daysFrom), z);
        Date end   = endOfDay(todayLD.plusDays(daysTo), z);

        return reservationRepository.findConfirmedOverlapping(hotelId, start, end).stream()
                .filter(r -> r.getDateFin() != null
                        && !r.getDateFin().before(start)
                        && r.getDateFin().before(end))
                .sorted(Comparator.comparing(Reservation::getDateFin))
                .toList();
    }

    /* ========= Réservations en attente (paginé) ========= */
    public Page<Reservation> getPending(Long hotelId, int page, int size) {
        Pageable p = PageRequest.of(Math.max(page,0), Math.max(size,1),
                Sort.by(Sort.Direction.DESC, "dateReservation"));
        return reservationRepository.findByChambre_Hotel_IdAndStatut(hotelId, Statut.EN_ATTENTE, p);
    }
    public DashboardSummaryDTO buildDashboardSummary(Long hotelId, String timezone) {
        ZoneId zone = (timezone == null || timezone.isBlank()) ? ZoneId.systemDefault() : ZoneId.of(timezone);

        Date rangeStart = firstDayThisMonth(zone);
        Date rangeEnd   = new Date();
        Date dToday     = today(zone);

        long roomsCount         = chambreRepository.countByHotelId(hotelId);
        long occupiedRoomsToday = reservationRepository.countChambresOccupeesJour(hotelId, dToday);
        double occupancyRate    = (roomsCount == 0) ? 0.0 : (100.0 * occupiedRoomsToday / roomsCount);

        long pendingCount   = reservationRepository.countReservationsEnAttente(hotelId);
        long checkinsToday  = reservationRepository.countReservationsJour(hotelId, dToday);

        long checkoutsToday = 0L;
        try {
            checkoutsToday = reservationRepository.countCheckoutsJour(hotelId, dToday);
        } catch (Exception ignored) {}

        long nightsSoldInRange = 0L;
        List<Reservation> confirmedOverlap = reservationRepository.findConfirmedOverlapping(hotelId, rangeStart, rangeEnd);
        for (Reservation r : confirmedOverlap) {
            if (r.getDateDebut() == null || r.getDateFin() == null) continue;
            nightsSoldInRange += nightsOverlap(rangeStart, rangeEnd, r.getDateDebut(), r.getDateFin());
        }

        double revenueInRange = 0.0; // placeholder

        return DashboardSummaryDTO.builder()
                .hotelId(hotelId)
                .hotelName(null)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .timezone(zone.getId())   // << echo timezone
                .generatedAt(new Date())  // << horodatage génération
                .roomsCount(roomsCount)
                .occupiedRoomsToday(occupiedRoomsToday)
                .occupancyRateToday(r(occupancyRate, 1))
                .pendingCount(pendingCount)
                .checkinsToday(checkinsToday)
                .checkoutsToday(checkoutsToday)
                .nightsSoldInRange(nightsSoldInRange)
                .revenueInRange(revenueInRange)
                .build();
    }

}
