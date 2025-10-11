package tn.esprit.gestionhotilere.dto;

import lombok.*;
import tn.esprit.gestionhotilere.entity.TypeChambre;
import tn.esprit.gestionhotilere.entity.VueChambre;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChambreOptionDTO {
    private Long id; // null en création, rempli en lecture
    private Long chambreId; // null en création, rempli en lecture
    private String description;
    private TypeChambre typeChambre;
    private VueChambre vue;
    private BigDecimal prixParNuitParPersonne;
    private boolean actif;
}
