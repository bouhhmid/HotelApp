package tn.esprit.gestionhotilere.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.gestionhotilere.entity.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUtilisateurIdOrderByDateEnvoiDesc(Long userId);
    long countByUtilisateur_IdAndLuFalse(Long userId);                        // ➕ badge non-lues

    Page<Notification> findByUtilisateur_IdOrderByDateEnvoiDesc(Long userId, Pageable pageable); // ➕ pagination
}
