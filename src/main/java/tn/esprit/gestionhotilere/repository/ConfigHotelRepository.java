package tn.esprit.gestionhotilere.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.gestionhotilere.entity.ConfigHotel;

public interface ConfigHotelRepository  extends JpaRepository<ConfigHotel, Long> {
    ConfigHotel findByHotelId(long hotelId);

}
