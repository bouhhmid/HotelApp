package tn.esprit.gestionhotilere.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PointXY {
    private String x; // date "YYYY-MM-DD" ou mois "YYYY-MM"
    private double y;
}
