package tn.esprit.gestionhotilere.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String typeEmail;

    @Column(name = "contenu", columnDefinition = "TEXT", nullable = false)
    private String contenu;

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;
}
