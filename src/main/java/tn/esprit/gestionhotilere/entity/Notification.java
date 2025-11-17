    package tn.esprit.gestionhotilere.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Column(length = 1000)
    private String message;

    private boolean lu;

    private Instant dateEnvoi;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private User utilisateur;


    @Column(name = "hotel_id")
    private Long hotelId;              // pour filtrer par hôtel dans le back

    @Column(name = "reservation_id")
    private Long reservationId;        // pour ouvrir la demande depuis la notif

    @Column(name = "target_role", length = 16)
    private String targetRole = "ADMIN";  // "ADMIN" ou "CLIENT" (simple & efficace)
}