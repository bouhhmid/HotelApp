package tn.esprit.gestionhotilere.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.gestionhotilere.entity.EmailTemplate;

import java.util.Optional;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    Optional<EmailTemplate> findByTypeEmailAndHotelId(String typeEmail, Long hotelId);
    Optional<EmailTemplate> findByTypeEmailAndHotelIdIsNull(String typeEmail);

}
