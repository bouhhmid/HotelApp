// tn/esprit/gestionhotilere/service/HousekeepingService.java
package tn.esprit.gestionhotilere.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.gestionhotilere.dto.HousekeepingTaskDTO;
import tn.esprit.gestionhotilere.repository.ReservationRepository;

import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HousekeepingService {

    private final ReservationRepository reservationRepository;

    public List<HousekeepingTaskDTO> tasksForToday(Long hotelId, ZoneId tz) {
        LocalDate today = LocalDate.now(tz);
        Date d0 = Date.from(today.atStartOfDay(tz).toInstant()); // on compare par jour entier

        var out = new ArrayList<HousekeepingTaskDTO>();

        // Départs (priorité haute, due 12:00)
        reservationRepository.findDeparturesOnDate(hotelId, d0).forEach(r ->
                out.add(task(num(r), "DEPARTURE", 1, "12:00"))
        );

        // Arrivées (priorité haute, due 14:00)
        reservationRepository.findArrivalsOnDate(hotelId, d0).forEach(r ->
                out.add(task(num(r), "ARRIVAL", 1, "14:00"))
        );

        // Stayovers (priorité normale, due 16:00)
        reservationRepository.findStayoversOnDate(hotelId, d0).forEach(r ->
                out.add(task(num(r), "STAYOVER", 2, "16:00"))
        );

        return out;
    }

    private String num(tn.esprit.gestionhotilere.entity.Reservation r) {
        return (r.getChambre() != null && r.getChambre().getNumero() != null)
                ? r.getChambre().getNumero() : "—";
    }

    private HousekeepingTaskDTO task(String room, String type, int prio, String due) {
        return HousekeepingTaskDTO.builder()
                .room(room).type(type).priority(prio).dueTime(due).build();
    }
}
