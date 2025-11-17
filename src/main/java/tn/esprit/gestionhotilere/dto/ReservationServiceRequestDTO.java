// ReservationServiceRequestDTO.java
package tn.esprit.gestionhotilere.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record ReservationServiceRequestDTO(
        @NotNull Long serviceHotelId,
        @NotNull LocalDateTime dateTime,   // date + heure du créneau
        @Positive int participants         // nombre de personnes
) {}
