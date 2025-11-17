package tn.esprit.gestionhotilere.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class ReservationChambreQuoteDTO {
    private long nuits;
    private int capaciteRequise; // nbAdultes + nbEnfants
    private int capaciteMax;     // nbChambres * (capAdulte + capEnfant)
    private boolean capacityOk;

    private BigDecimal subtotal;
    private BigDecimal taxes;
    private BigDecimal frais;
    private BigDecimal total;
    private String currency = "TND";
}
