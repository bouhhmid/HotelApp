package tn.esprit.gestionhotilere.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter
public class ReservationChambreRequestDTO {

    private Long chambreId;

    @NotNull @FutureOrPresent
    private LocalDateTime dateDebut;

    @NotNull @Future
    private LocalDateTime dateFin;

    @Positive
    private int nbAdultes = 1;

    @PositiveOrZero
    private int nbEnfants = 0;

    @Positive
     private int nbChambres = 1;

    private String VueId;

}
