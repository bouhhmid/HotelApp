package tn.esprit.gestionhotilere.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hotels")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String adresse;
    private String description;
    private String etoiles;


    @Column(name = "image_url")
    private String imageUrl;
    @Column(nullable = true)
    private Double latitude;

    @Column(nullable = true)
    private Double longitude;




    @ManyToOne
    @JoinColumn(name = "admin_id")
    @JsonIgnore
    private User admnistrateur;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL)
   // @JsonIgnore
    private List<Chambre> chambres;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL)
    // @JsonIgnore
    private List<ServiceHotel> serviceHotels;

    @OneToOne(mappedBy = "hotel", cascade = CascadeType.ALL)
    @JsonIgnore
    private ConfigHotel configHotel;
}
