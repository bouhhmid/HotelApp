package tn.esprit.gestionhotilere.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.gestionhotilere.entity.Hotel;
import tn.esprit.gestionhotilere.entity.Role;
import tn.esprit.gestionhotilere.entity.User;
import tn.esprit.gestionhotilere.repository.HotelRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelContextService {

    private final UserService userService;
    private final HotelRepository hotelRepository;



    public List<Hotel> getHotelsForCurrentUser() {
        User current = userService.getOrCreateCurrentUser();

        if (current.getRole() == Role.SUPERADMIN) {
            return hotelRepository.findAll();
        }

        List<Hotel> hotels = hotelRepository.findByAdmnistrateur_Id(current.getId());
        if (hotels.isEmpty()) {
            throw new RuntimeException("Aucun hôtel associé à cet administrateur");
        }
        return hotels;
    }
}
