package tn.esprit.gestionhotilere.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "chambre_options",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_option_chambre_type_vue",
                columnNames = {"chambre_id", "type_chambre", "vue"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChambreOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "chambre_id", nullable = false)
    private Chambre chambre;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_chambre", nullable = false, length = 32)
    private TypeChambre typeChambre;

    @Enumerated(EnumType.STRING)
    @Column(name = "vue", nullable = false, length = 32)
    private VueChambre vue;

    @Column(name = "prix_par_nuit_par_personne", precision = 18, scale = 3, nullable = false)
    private BigDecimal prixParNuitParPersonne;

    @Column(nullable = false)
    private boolean actif = true;
}
