package tn.esprit.gestionhotilere.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionhotilere.dto.ChambreDetailsDTO;
import tn.esprit.gestionhotilere.dto.ChambreOptionDTO;
import tn.esprit.gestionhotilere.dto.ChambrecreateDTO;
import tn.esprit.gestionhotilere.service.ChambreService;
import tn.esprit.gestionhotilere.service.ReservationService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chambres")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ChambreController {

    private final ChambreService chambreService;
    private final ReservationService reservationService;

    @PostMapping("/ajouter/{hotelId}")
   public ResponseEntity<ChambreDetailsDTO> ajouterChambre(
            @PathVariable Long hotelId,
            @RequestBody ChambrecreateDTO dto) {
        return ResponseEntity.ok(chambreService.ajouterChambre(hotelId, dto));
    }

    @PutMapping("/modifier/{chambreId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<ChambreDetailsDTO> modifierChambre(
            @PathVariable Long chambreId,
            @RequestBody ChambrecreateDTO dto) {
        return ResponseEntity.ok(chambreService.modifierChambre(chambreId, dto));
    }

    @DeleteMapping("/supprimer/{chambreId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<Void> supprimerChambre(@PathVariable Long chambreId) {
        chambreService.supprimerChambre(chambreId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<ChambreDetailsDTO>> getChambresParHotel(@PathVariable Long hotelId) {
        return ResponseEntity.ok(chambreService.getChambresByHotel(hotelId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChambreDetailsDTO> getChambreById(@PathVariable Long id) {
        return ResponseEntity.ok(chambreService.getChambreById(id));
    }

    @GetMapping("{chambreId}/disponibilites")
    public ResponseEntity<List<String>> getDatesDisponibles(@PathVariable Long chambreId) {
        List<LocalDate> dates = reservationService.getDatesDisponibles(chambreId);
        List<String> result = dates.stream().map(LocalDate::toString).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{chambreId}/options")
    public ResponseEntity<List<ChambreOptionDTO>> listOptions(@PathVariable Long chambreId) {
        return ResponseEntity.ok(reservationService.listOptionsByChambre(chambreId));
    }
    @PatchMapping("/{chambreId}/dirty")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public void markDirty(@PathVariable Long chambreId) { chambreService.markDirty(chambreId); }

    @PatchMapping("/{chambreId}/clean")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','HOUSEKEEPING')")
    public void markClean(@PathVariable Long chambreId) { chambreService.markClean(chambreId); }
}
