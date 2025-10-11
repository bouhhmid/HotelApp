package tn.esprit.gestionhotilere.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HousekeepingTaskDTO {
    private String room;     // Numéro de chambre
    private String type;     // ARRIVAL | DEPARTURE | STAYOVER
    private int priority;    // 1 = haut, 2 = normal
    private String dueTime;  // "12:00" / "14:00" / "16:00"
}
