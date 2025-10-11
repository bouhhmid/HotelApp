package tn.esprit.gestionhotilere.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "config_hotel")
public class ConfigHotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;
    private String logo;
    private String couleurTheme;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;
}
