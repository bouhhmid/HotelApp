package tn.esprit.gestionhotilere.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "reviews")
@Getter
@Setter
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long hotelId;
    private String userName;
    private Double score;
    private String title;
    @Column(length = 2000)
    private String comment;
    private Double proprete;
    private Double emplacement;
    private Double confort;
    private Double qualitePrix;
    private Double wifi;

    private Instant createdAt = Instant.now();
    // getters/setters
}
