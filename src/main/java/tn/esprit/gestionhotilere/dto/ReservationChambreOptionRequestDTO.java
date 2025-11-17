package tn.esprit.gestionhotilere.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationChambreOptionRequestDTO {
    @NotNull
    private Long optionId;
    @NotNull @FutureOrPresent
    private LocalDateTime dateDebut;
    @NotNull @Future
    private LocalDateTime dateFin;
    @Min(1) private int nbAdultes = 1;
    @Min(0) private int nbEnfants = 0;
    @Min(1) private int nbChambres = 1;
}
