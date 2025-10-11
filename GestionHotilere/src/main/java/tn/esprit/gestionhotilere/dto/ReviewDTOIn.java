package tn.esprit.gestionhotilere.dto;

import java.time.Instant;

public record ReviewDTOIn(
        Double score,
        String title,
        String comment,
        Double proprete,
        Double emplacement,
        Double confort,
        Double qualitePrix,
        Double wifi
) {}
