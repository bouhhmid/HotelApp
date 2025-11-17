package tn.esprit.gestionhotilere.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.ManyToOne;

import java.util.Date;

@Getter
@Setter
@Table(name = "service_hotel")
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})

public class ServiceHotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomService ;
    private String type ;
    private String description ;
    private double prix ;
    private int capacite ;
    private Date disponibilte ;

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;



}
