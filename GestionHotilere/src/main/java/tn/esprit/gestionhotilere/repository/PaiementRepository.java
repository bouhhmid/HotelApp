package tn.esprit.gestionhotilere.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.gestionhotilere.entity.Paiement;

import java.util.Optional;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    Optional<Paiement> findByPaymentIntentId(String paymentIntentId);

}
