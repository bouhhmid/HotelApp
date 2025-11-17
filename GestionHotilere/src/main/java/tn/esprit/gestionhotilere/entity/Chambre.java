package tn.esprit.gestionhotilere.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "chambres",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_chambre_hotel_numero",
                columnNames = {"hotel_id", "numero"}
        )
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chambre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_chambre", nullable = false, length = 32)
    private TypeChambre typeChambre = TypeChambre.SIMPLE;

    @Column(name = "prix_base", precision = 18, scale = 3)
    private BigDecimal prixBase;

    @Column(nullable = false)
    private boolean dispo = true;

    @Column(length = 5000)
    private String description;

    @Column(length = 2024)
    private String imageUrl;

    @Column(name = "capacite_adulte", nullable = false)
    private Integer capaciteAdulte = 2;

    @Column(name = "capacite_enfant", nullable = false)
    private Integer capaciteEnfant = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    @JsonIgnore
    private Hotel hotel;

    @OneToMany(mappedBy = "chambre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChambreOption> options = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EtatChambre etatChambre = EtatChambre.CLEAN;
    private LocalDateTime lastCleanedAt;
}
