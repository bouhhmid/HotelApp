// ReservationServiceQuoteDTO.java
package tn.esprit.gestionhotilere.dto;

import java.math.BigDecimal;

public record ReservationServiceQuoteDTO(
        BigDecimal subtotal,
        BigDecimal taxes,
        BigDecimal frais,
        BigDecimal total,
        String currency,
        boolean capacityOk
) {}
