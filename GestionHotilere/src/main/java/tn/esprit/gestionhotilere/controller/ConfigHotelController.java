package tn.esprit.gestionhotilere.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionhotilere.entity.ConfigHotel;
import tn.esprit.gestionhotilere.service.ConfigHotelService;

@RestController
@RequestMapping("/api/config-hotel")
@RequiredArgsConstructor
public class ConfigHotelController {

    private final ConfigHotelService configHotelService;

    @GetMapping("/{hotelId}")
    public ResponseEntity<ConfigHotel> getConfigByHotel(@PathVariable Long hotelId) {

        return ResponseEntity.ok(configHotelService.getConfigByHotel(hotelId));
    }

    @PostMapping("/{hotelId}")
    public ResponseEntity<ConfigHotel> updateOrCreateConfig(
            @PathVariable Long hotelId,
            @RequestBody ConfigHotel configHotel) {
        return ResponseEntity.ok(configHotelService.ajouterOuModifierConfig(hotelId, configHotel));
    }

    @PutMapping("/{id}")
    public ConfigHotel updateConfig(@PathVariable Long id, @RequestBody ConfigHotel config) {
        return configHotelService.modifierConfig(id, config);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public void deleteConfig(@PathVariable Long id) {
        configHotelService.supprimerConfig(id);

    }
}
