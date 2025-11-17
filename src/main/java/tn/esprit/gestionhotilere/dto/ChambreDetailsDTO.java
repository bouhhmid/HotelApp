package tn.esprit.gestionhotilere.dto;

import lombok.*;
import tn.esprit.gestionhotilere.entity.EtatChambre;
import tn.esprit.gestionhotilere.entity.TypeChambre;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChambreDetailsDTO {
    private Long id;
    private String numero;
    private TypeChambre typeChambre;
    private EtatChambre etatChambre;
    private LocalDateTime lastClenedAt;
    private boolean dispo;
    private String description;
    private String imageUrl;
    private Integer capaciteAdulte;
    private Integer capaciteEnfant;
    private List<ChambreOptionDTO> options;
}
