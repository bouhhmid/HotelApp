package tn.esprit.gestionhotilere.dto;


import lombok.*;
import tn.esprit.gestionhotilere.entity.TypeChambre;

import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChambrecreateDTO {


    @NotBlank
    private String numero;

    @NotNull
    private TypeChambre typeChambre;

    @NotNull
    @Min(1)
    private Integer capaciteAdulte;

    @NotNull
    @Min(0)
    private Integer capaciteEnfant;

    private String description;
    private String imageUrl;
    @Builder.Default
    private List<ChambreOptionDTO> options = new ArrayList<>(); // options créées avec la chambre

}
