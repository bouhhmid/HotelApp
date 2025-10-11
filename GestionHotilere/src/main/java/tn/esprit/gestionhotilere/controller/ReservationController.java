package tn.esprit.gestionhotilere.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionhotilere.dto.*;
import tn.esprit.gestionhotilere.entity.Reservation;
import tn.esprit.gestionhotilere.entity.Statut;
import tn.esprit.gestionhotilere.service.ReservationService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ReservationController {

    private final ReservationService reservationService;

    // ==================== CHAMBRE ====================

    @PostMapping("/chambre/{chambreId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ReservationChambreResponseDTO> reserverChambre(
            @PathVariable Long chambreId,
            @Valid @RequestBody ReservationChambreRequestDTO dto
    ) {
        Reservation saved = reservationService.reserverChambre(
                chambreId,
                Date.from(dto.getDateDebut().atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(dto.getDateFin().atZone(ZoneId.systemDefault()).toInstant())
        );

        // Réponse enrichie (hôtel/chambre)
        ReservationChambreResponseDTO resp = toDto(saved);
        if (saved.getChambre() != null && saved.getChambre().getHotel() != null) {
            resp.setHotelId(saved.getChambre().getHotel().getId());
            resp.setNomHotel(saved.getChambre().getHotel().getNom());
            resp.setChambreId(saved.getChambre().getId());
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/chambres/quote")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ReservationChambreQuoteDTO> quoteChambre(
            @Valid @RequestBody ReservationChambreRequestDTO dto
    ) {
        return ResponseEntity.ok(reservationService.quoteChambre(dto));
    }

    @PostMapping("/chambres")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ReservationChambreResponseDTO> createChambre(
            @Valid @RequestBody ReservationChambreRequestDTO dto
    ) {
        return ResponseEntity.ok(reservationService.createChambreReservation(dto));
    }

    // ==================== SERVICE HOTEL ====================

    @PostMapping("/service/{serviceId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ReservationChambreResponseDTO> reserverService(
            @PathVariable Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin
    ) {
        Reservation saved = reservationService.reserverService(serviceId, dateDebut, dateFin);

        // Pas besoin de setHotel ici : le service l'a déjà fait.
        ReservationChambreResponseDTO resp = toDto(saved);
        if (saved.getServiceHotel() != null && saved.getServiceHotel().getHotel() != null) {
            resp.setHotelId(saved.getServiceHotel().getHotel().getId());
            resp.setNomHotel(saved.getServiceHotel().getHotel().getNom());
            resp.setServiceId(saved.getServiceHotel().getId());
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/services/quote")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ReservationServiceQuoteDTO> quoteService(
            @Valid @RequestBody ReservationServiceRequestDTO dto
    ) {
        return ResponseEntity.ok(reservationService.quoteService(dto));
    }

    @PostMapping("/services")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ReservationChambreResponseDTO> createService(
            @Valid @RequestBody ReservationServiceRequestDTO dto
    ) {
        Reservation saved = reservationService.reserverServiceV2(dto);
        ReservationChambreResponseDTO resp = toDto(saved);
        if (saved.getServiceHotel() != null && saved.getServiceHotel().getHotel() != null) {
            resp.setHotelId(saved.getServiceHotel().getHotel().getId());
            resp.setNomHotel(saved.getServiceHotel().getHotel().getNom());
            resp.setServiceId(saved.getServiceHotel().getId());
        }
        return ResponseEntity.ok(resp);
    }

    // ==================== ACTIONS ====================

    @PutMapping("/{reservationId}/annuler")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN','SUPERADMIN')") // ✅ ADMIN autorisé aussi
    public ResponseEntity<Void> annuler(@PathVariable Long reservationId) {
        reservationService.annulerReservation(reservationId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{reservationId}/confirmer")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<ReservationChambreResponseDTO> confirmer(@PathVariable Long reservationId) {
        Reservation saved = reservationService.confirmerReservation(reservationId);
        return ResponseEntity.ok(toDto(saved));
    }

    // ==================== LISTES ====================

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Page<ReservationChambreResponseDTO>> mesReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Statut statut
    ) {
        Page<Reservation> resultPage = reservationService.mesReservations(page, size, statut);

        Page<ReservationChambreResponseDTO> dtoPage = resultPage.map(r -> {
            var dto = toDto(r);
            if (r.getChambre() != null) {
                dto.setChambreId(r.getChambre().getId());
                if (r.getChambre().getHotel() != null) {
                    dto.setHotelId(r.getChambre().getHotel().getId());
                    dto.setNomHotel(r.getChambre().getHotel().getNom());
                }
            }
            if (r.getServiceHotel() != null) {
                dto.setServiceId(r.getServiceHotel().getId());
                if (r.getServiceHotel().getHotel() != null) {
                    dto.setHotelId(r.getServiceHotel().getHotel().getId());
                    dto.setNomHotel(r.getServiceHotel().getHotel().getNom());
                }
            }
            return dto;
        });

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/en-attente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Page<ReservationChambreResponseDTO>> listerEnAttente(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Reservation> resultPage = reservationService.listerEnAttente(page, size);

        Page<ReservationChambreResponseDTO> dtoPage = resultPage.map(this::toDtoWithHotel);
        return ResponseEntity.ok(dtoPage);
    }

    @PostMapping("/chambres/options/quote")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ReservationChambreQuoteDTO> quoteChambreOption(
            @Valid @RequestBody ReservationChambreOptionRequestDTO dto
    ) {
        return ResponseEntity.ok(reservationService.quoteChambreOption(dto));
    }

    @PostMapping("/chambres/options")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ReservationChambreResponseDTO> createChambreOption(
            @Valid @RequestBody ReservationChambreOptionRequestDTO dto
    ) {
        return ResponseEntity.ok(reservationService.createChambreReservationOption(dto));
    }

    @GetMapping("/hotel/{hotelId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Page<ReservationChambreResponseDTO>> listByHotel(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Statut statut
    ) {
        Page<Reservation> resultPage = reservationService.listerParHotel(hotelId, statut, page, size);
        Page<ReservationChambreResponseDTO> dtoPage = resultPage.map(this::toDtoWithHotel);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/en-attente/hotel/{hotelId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Page<ReservationChambreResponseDTO>> enAttenteByHotel(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return listByHotel(hotelId, page, size, Statut.EN_ATTENTE);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','CLIENT')")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        Reservation reservation = reservationService.getReservationById(id);
        return ResponseEntity.ok(reservation);
    }

    /* ==================== Helpers ==================== */

    private ReservationChambreResponseDTO toDto(Reservation r) {
        var dto = new ReservationChambreResponseDTO();
        dto.setId(r.getId());
        dto.setDateReservation(r.getDateReservation().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        dto.setDateDebut(r.getDateDebut().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        dto.setDateFin(r.getDateFin().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        dto.setStatut(r.getStatut());

        if (r.getChambre() != null) dto.setChambreId(r.getChambre().getId());
        if (r.getServiceHotel() != null) dto.setServiceId(r.getServiceHotel().getId());

        if (r.getChambre() != null && r.getChambre().getHotel() != null) {
            dto.setHotelId(r.getChambre().getHotel().getId());
            dto.setNomHotel(r.getChambre().getHotel().getNom());
        } else if (r.getServiceHotel() != null && r.getServiceHotel().getHotel() != null) {
            dto.setHotelId(r.getServiceHotel().getHotel().getId());
            dto.setNomHotel(r.getServiceHotel().getHotel().getNom());
        }
        return dto;
    }

    private ReservationChambreResponseDTO toDtoWithHotel(Reservation r) {
        return toDto(r);
    }
    @GetMapping("/chambres/{chambreId}/calendar")
    @PreAuthorize("permitAll()") // ou 'hasRole("CLIENT")' si tu veux restreindre
    public ResponseEntity<List<String>> calendar(@PathVariable Long chambreId) {
        var dates = reservationService.getDatesDisponibles(chambreId); // List<LocalDate>
        // On renvoie des ISO yyyy-MM-dd (facile à filtrer côté front)
        var out = dates.stream().map(java.time.LocalDate::toString).toList();
        return ResponseEntity.ok(out);
    }
    @PutMapping("/reservations/{reservationId}/checkout")
    public ResponseEntity<Void> checkout(@PathVariable Long reservationId) {
        reservationService.checkout(reservationId);
        return ResponseEntity.noContent().build(); // 204, aucun body => plus de problème Jackson
    }

}
