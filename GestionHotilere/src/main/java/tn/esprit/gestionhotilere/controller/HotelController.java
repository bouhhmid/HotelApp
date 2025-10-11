package tn.esprit.gestionhotilere.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionhotilere.dto.HotelDetailsDTO;
import tn.esprit.gestionhotilere.dto.HotelListDTO;
import tn.esprit.gestionhotilere.entity.Hotel;
import tn.esprit.gestionhotilere.service.HotelService;
import tn.esprit.gestionhotilere.service.UserService;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<Hotel> ajouterHotel(@RequestBody Hotel hotel) {
        return ResponseEntity.ok(hotelService.ajouterHotel(hotel));
    }

    @GetMapping
    public List<HotelListDTO> getAllHotels() {
        return hotelService.getAllHotels();
    }

    @GetMapping("/{id}")
    public HotelDetailsDTO getHotelById(@PathVariable Long id) {
        return hotelService.getHotelDetails(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public void supprimerHotel(@PathVariable Long id) {
        hotelService.supprimerHotel(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public Hotel modifierHotel(@PathVariable Long id, @RequestBody Hotel hotel) {
        return hotelService.updateHotel(hotel, id);
    }

    // HotelController.java
    @GetMapping("/my-hotels")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SUPERADMIN')")
    public List<HotelListDTO> getMyHotels() {
        var hotels = hotelService.getHotelsForCurrentAdmin(); // List<Hotel>

        return hotels.stream().map(h -> {
            var dto = new HotelListDTO();
            dto.setId(h.getId());
            dto.setNom(h.getNom());
            dto.setAdresse(h.getAdresse());
            dto.setEtoiles(h.getEtoiles());        // Integer
            dto.setImageUrl(h.getImageUrl());
            dto.setLatitude(h.getLatitude());
            dto.setLongitude(h.getLongitude());
            dto.setDescription(h.getDescription());
            return dto;
        }).toList(); // 200 OK + [] si aucun hôtel
    }
}

