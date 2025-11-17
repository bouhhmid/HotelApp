package tn.esprit.gestionhotilere.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionhotilere.entity.ServiceHotel;
import tn.esprit.gestionhotilere.service.HotelService;
import tn.esprit.gestionhotilere.service.ServiceHotelService;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")

public class ServiceHotelController {

    private final ServiceHotelService serviceHotelService;

    @PostMapping("/{hotelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ServiceHotel> ajouterService(
            @PathVariable Long hotelId,
            @RequestBody ServiceHotel service) {
        return ResponseEntity.ok(serviceHotelService.addService(hotelId, service));
    }

    @GetMapping("/{hotelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<ServiceHotel>> getServices(@PathVariable Long hotelId) {
        return ResponseEntity.ok(serviceHotelService.getServicesByHotel(hotelId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        serviceHotelService.supprimerService(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/hotel/{hotelId}/service/{serviceId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SUPERADMIN')")
    public ResponseEntity<ServiceHotel> modifierService(
            @PathVariable Long hotelId,
            @PathVariable Long serviceId,
            @RequestBody ServiceHotel servicePayload) {

        ServiceHotel updated = serviceHotelService.updateService(hotelId, serviceId, servicePayload);
        return ResponseEntity.ok(updated);
    }


}
