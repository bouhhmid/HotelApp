package tn.esprit.gestionhotilere.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.gestionhotilere.entity.Hotel;

import java.util.List;
import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

     List<Hotel> findByAdresseContainingIgnoreCase(String adresse);

     // Par ID interne de l'admin
     List<Hotel> findByAdmnistrateur_Id(Long adminId);

     // Par id Keycloak (idk)
     List<Hotel> findByAdmnistrateur_Idk(String idk);

     Optional<Hotel> findById(Long id);
}

