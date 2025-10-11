package tn.esprit.gestionhotilere.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.gestionhotilere.entity.ConfigHotel;
import tn.esprit.gestionhotilere.entity.Hotel;
import tn.esprit.gestionhotilere.entity.Role;
import tn.esprit.gestionhotilere.entity.User;
import tn.esprit.gestionhotilere.repository.ConfigHotelRepository;
import tn.esprit.gestionhotilere.repository.HotelRepository;

import static java.util.Collections.replaceAll;

@Service
@RequiredArgsConstructor
@Transactional
public class ConfigHotelService {
    @Autowired
    private ConfigHotelRepository configHotelRepository;
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private UserService userService;
    public ConfigHotel getConfigByHotel(Long hotelId) {
        Long userId = userService.getOrCreateCurrentUser().getId();
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hôtel introuvable"));
        User user = userService.getByIdkc(userId.toString());
        if (!hotel.getAdmnistrateur().getId().equals(userId)
                && user.getRole() != Role.SUPERADMIN) {
            throw new RuntimeException("Accès refusé");
        }
        return configHotelRepository.findByHotelId(hotelId);
    }
    public ConfigHotel ajouterOuModifierConfig(Long hotelId, ConfigHotel config) {
        Long userId = userService.getOrCreateCurrentUser().getId();
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hôtel introuvable"));
        if (!hotel.getAdmnistrateur().getId().equals(userId)
                && userService.getByIdkc(userId.toString()).getRole() != Role.SUPERADMIN) {
            throw new RuntimeException("Accès refusé");
        }
        hotel.setConfigHotel(config);
        config.setUrl(genererUrl(hotel));
        config.setHotel(hotel);
        return configHotelRepository.save(config);
    }

     public ConfigHotel modifierConfig(Long id, ConfigHotel newConfig) {
        ConfigHotel config = configHotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Config introuvable"));

        config.setCouleurTheme(newConfig.getCouleurTheme());
        config.setLogo(newConfig.getLogo());
        config.setUrl(newConfig.getUrl());
        return configHotelRepository.save(config);
    }
    public void supprimerConfig(Long id) {
        ConfigHotel config = configHotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Config introuvable"));

        if (config.getHotel() != null) {
            config.getHotel().setConfigHotel(null); // on casse le lien
        }

        configHotelRepository.delete(config);    }


     private  String genererUrl(Hotel hotel){
        String nom = hotel.getNom().toLowerCase()
         .replaceAll("\\s+", "-")
                 .replaceAll("[^a-z0-9\\-]", "");

         return "https://monapp.com/hotels/" + nom;

     }

}
