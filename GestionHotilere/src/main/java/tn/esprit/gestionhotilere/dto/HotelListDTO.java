package tn.esprit.gestionhotilere.dto;

import lombok.Data;

@Data
public class HotelListDTO {
    private Long id;
    private String nom;
    private String etoiles;
    private String imageUrl;
    private String description;
    private String adresse;
    private Double latitude;
    private Double longitude;
}
