package tn.esprit.gestionhotilere.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import tn.esprit.gestionhotilere.dto.HotelDetailsDTO;
import tn.esprit.gestionhotilere.dto.HotelListDTO;
import tn.esprit.gestionhotilere.entity.Hotel;
import tn.esprit.gestionhotilere.entity.Role;
import tn.esprit.gestionhotilere.entity.User;
import tn.esprit.gestionhotilere.repository.HotelRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final UserService userService;

    public List<HotelListDTO> getAllHotels() {
        return hotelRepository.findAll().stream().map(hotel -> {
            HotelListDTO dto = new HotelListDTO();
            dto.setId(hotel.getId());
            dto.setNom(hotel.getNom());
            dto.setEtoiles(hotel.getEtoiles());
            dto.setImageUrl(hotel.getImageUrl());
            dto.setDescription(hotel.getDescription());
            dto.setAdresse(hotel.getAdresse());
            dto.setLatitude(hotel.getLatitude());
            dto.setLongitude(hotel.getLongitude());


            return dto;
        }).collect(Collectors.toList());
    }

    public Hotel ajouterHotel(Hotel hotel) {
        User admin = userService.getOrCreateCurrentUser();
        hotel.setAdmnistrateur(admin);

        if (hotel.getChambres() != null) {
            hotel.getChambres().forEach(c -> c.setHotel(hotel));
        }

        if (hotel.getServiceHotels() != null) {
            hotel.getServiceHotels().forEach(s -> s.setHotel(hotel)); // ✅ ici tu peux appeler setHotel
        }


        return hotelRepository.save(hotel);
    }


    public Hotel getHotelById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hôtel introuvable"));
    }

    public void supprimerHotel(Long id) {
        verifierDroit(id);
        hotelRepository.deleteById(id);
    }

    public Hotel updateHotel(Hotel hotel, Long id) {
        verifierDroit(id);

        Optional<Hotel> hotel1 = hotelRepository.findById(id);
        if (hotel1.isPresent()) {
            Hotel hotelexists = hotel1.get();
            hotelexists.setNom(hotel.getNom());
            hotelexists.setAdresse(hotel.getAdresse());
            hotelexists.setDescription(hotel.getDescription());
            hotelexists.setEtoiles(hotel.getEtoiles());
            return hotelRepository.save(hotelexists);
        } else {
            throw new RuntimeException("Hôtel introuvable");
        }
    }

    public List<Hotel> getHotelsForCurrentAdmin() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idk = jwt.getSubject();
        return hotelRepository.findByAdmnistrateur_Idk(idk);
    }

    // 🔐 Vérifie si l'utilisateur connecté a le droit d'agir sur cet hôtel
    private void verifierDroit(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hôtel introuvable"));

        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idk = jwt.getSubject();

        User currentUser = userService.getByIdkc(idk);

        if (currentUser.getRole() == Role.SUPERADMIN) return;

        if (!hotel.getAdmnistrateur().getIdk().equals(idk)) {
            throw new RuntimeException("Accès refusé : vous n’êtes pas administrateur de cet hôtel.");
        }
    }
    public HotelDetailsDTO getHotelDetails(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hôtel introuvable"));

        HotelDetailsDTO dto = new HotelDetailsDTO();
        dto.setId(hotel.getId());
        dto.setNom(hotel.getNom());
        dto.setAdresse(hotel.getAdresse());
        dto.setDescription(hotel.getDescription());
        dto.setEtoiles(hotel.getEtoiles());
        dto.setImageUrl(hotel.getImageUrl());
        dto.setLatitude(hotel.getLatitude());
        dto.setLongitude(hotel.getLongitude());

        // ✅ Mapping des chambres
        List<HotelDetailsDTO.ChambreDTO> chambres = hotel.getChambres().stream()
                .map(c -> {
                    HotelDetailsDTO.ChambreDTO cdto = new HotelDetailsDTO.ChambreDTO();
                    cdto.setId(c.getId());
                    cdto.setNumero(c.getNumero());
                    cdto.setPrix(c.getPrixBase());
                    cdto.setDispo(c.isDispo());
                    cdto.setImageUrl(c.getImageUrl());
                    cdto.setTypeChambre(c.getTypeChambre() != null ? c.getTypeChambre().name() : null);
                    return cdto;
                })
                .collect(Collectors.toList());


        // ✅ Mapping des services
        List<HotelDetailsDTO.ServiceHotelDTO> services = hotel.getServiceHotels().stream()
                .map(s -> {
                    HotelDetailsDTO.ServiceHotelDTO sdto = new HotelDetailsDTO.ServiceHotelDTO();
                    sdto.setId(s.getId());
                    sdto.setNomService(s.getNomService());
                    sdto.setType(s.getType());
                    sdto.setDescription(s.getDescription());
                    sdto.setPrix(s.getPrix());
                    sdto.setCapacite(s.getCapacite());
                    sdto.setDisponibilte(s.getDisponibilte());
                    return sdto;
                })
                .collect(Collectors.toList());
        dto.setServiceHotels(services);

        return dto;
    }


}
