// tn/esprit/gestionhotilere/dto/DashboardSummaryDTO.java
package tn.esprit.gestionhotilere.dto;

import lombok.*;

import java.util.Date;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class DashboardSummaryDTO {
    private Long hotelId;
    private String hotelName;
    private Date rangeStart;
    private Date rangeEnd;

    // echo & debug
    private String timezone;
    private Date generatedAt;

    // KPIs de stock/flux
    private long roomsCount;
    private long occupiedRoomsToday;
    private double occupancyRateToday;

    private long pendingCount;
    private long checkinsToday;
    private long checkoutsToday;

    private long nightsSoldInRange;
    private double revenueInRange;
}
