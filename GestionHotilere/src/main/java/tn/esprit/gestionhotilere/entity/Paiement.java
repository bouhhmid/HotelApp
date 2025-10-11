package tn.esprit.gestionhotilere.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
@Entity
@Table(name = "paiements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double montant;

    private String devise; // Exemple : "eur", "usd"

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutPaiement statut;


    private String paymentIntentId; // Stripe PaymentIntent ID

    @Temporal(TemporalType.TIMESTAMP)
    private Date datePaiement;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    @JsonIgnore
    private Reservation reservation;
}
