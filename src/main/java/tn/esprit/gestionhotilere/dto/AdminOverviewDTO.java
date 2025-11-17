package tn.esprit.gestionhotilere.dto;
import lombok.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminOverviewDTO {
    private double tauxOccupationJour;
    private long nbReservationsEnAttente;
    private long nbReservationsJour;

    // Séries
    private List<PointXY> reservations7J;
    private double avgLengthOfStay;
    private double cancelRate;
    private Map<String, Double> occupancyByType;
    private double revenuMois;
    private List<PointXY> revenu12M;

}