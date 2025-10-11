package tn.esprit.gestionhotilere.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import tn.esprit.gestionhotilere.entity.ServiceHotel;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
@Getter
@Setter
public class HotelDetailsDTO {
    private Long id;
    private String nom;
    private String adresse;
    private String description;
    private String etoiles;
    private String imageUrl;
    private List<ServiceHotelDTO> serviceHotels;
    private List<ChambreDTO> chambres;
    private double latitude;
    private double longitude;

    @Data
    public static class ChambreDTO {
        private Long id;
        private String numero;
        private BigDecimal prix;
        private boolean dispo;
        private String imageUrl;
        private String typeChambre;
        private String description;
        private int capciteAdulte;
        private int capciteEnfant;
    }
    @Data
    public static class ServiceHotelDTO {
        private Long id;
        private String nomService;
        private String type;
        private String description;
        private double prix;
        private int capacite;
        private Date disponibilte;
    }
}
