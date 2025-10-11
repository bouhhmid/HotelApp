package tn.esprit.gestionhotilere.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Métadonnées
    @Column(name = "date_reservation", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateReservation;

    // --- Pour CHAMBRE (plage de dates)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_debut")
    private Date dateDebut;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_fin")
    private Date dateFin;

    // --- Pour SERVICE (créneau unique)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_time")
    private Date dateTime;

    // --- Statut
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut;

    // --- Capacités
    @Min(0)
    @Column(name = "nb_enfants")
    private Integer nbEnfants;       // null si service

    @Min(1)
    @Column(name = "nb_adultes")
    private Integer nbAdultes;       // null si service

    @Min(1)
    @Column(name = "nb_chambres")
    private Integer nbChambres;      // null si service

    @Min(1)
    @Column(name = "participants")
    private Integer participants;    // null si chambre

    // --- Montant
    @NotNull
    @Column(name = "total_amount", precision = 18, scale = 3)
    private BigDecimal totalAmount = BigDecimal.ZERO; // millimes supportés

    @NotNull
    @Column(name = "currency", length = 10)
    private String currency = "TND";

    // --- Liens
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chambre_id")
    private Chambre chambre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_hotel_id")
    private ServiceHotel serviceHotel;

    // --- Nouvelle relation avec l'Hôtel
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Paiement paiement;

    // --- Règles d’intégrité
    @PrePersist
    private void onCreate() {
        if (dateReservation == null) dateReservation = new Date();
        validateReservation();
    }

    @PreUpdate
    private void onUpdate() {
        validateReservation();
    }

    private void validateReservation() {
        boolean hasChambre = chambre != null;
        boolean hasService = serviceHotel != null;

        // Exactement un des deux
        if (!(hasChambre ^ hasService)) {
            throw new IllegalStateException(
                    "La réservation doit porter soit sur une chambre, soit sur un service, pas les deux."
            );
        }

        if (hasChambre) {
            // ── Cas CHAMBRE ─────────────────────────────────────────
            if (dateDebut == null || dateFin == null || !dateDebut.before(dateFin)) {
                throw new IllegalStateException("Réservation chambre : dateDebut/dateFin invalides.");
            }
            // nbChambres peut être nul => considérer 1 par défaut
            if (nbChambres != null && nbChambres < 1) {
                throw new IllegalStateException("Réservation chambre : nbChambres doit être >= 1.");
            }
        } else {
            // ── Cas SERVICE ─────────────────────────────────────────
            if (dateTime == null) {
                throw new IllegalStateException("Réservation de service : dateTime obligatoire.");
            }
            // Pour un service, on NE REQUIERT PAS nbAdultes/nbEnfants/nbChambres
            // dateDebut/dateFin peuvent être renseignés (créneau) mais ne sont pas obligatoires ici.
            if (participants != null && participants < 1) {
                throw new IllegalStateException("Réservation de service : participants doit être >= 1.");
            }
        }
    }
}
