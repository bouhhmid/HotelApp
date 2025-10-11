package tn.esprit.gestionhotilere.dto;

import lombok.Getter;
import lombok.Setter;
import tn.esprit.gestionhotilere.entity.Statut;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationChambreResponseDTO {
    private Long id;
    private LocalDateTime dateReservation;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private Statut statut;
    private Long chambreId;
    private Long serviceId;
    private Long HotelId;
    private String nomHotel;

    private int nbAdultes;
    private int nbEnfants;
    private int nbChambres;

    private BigDecimal subtotal;
    private BigDecimal taxes;
    private BigDecimal frais;
    private BigDecimal total;
    private String currency = "TND";
}
