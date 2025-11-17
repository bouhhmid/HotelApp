package tn.esprit.gestionhotilere.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.gestionhotilere.entity.ServiceHotel;
import tn.esprit.gestionhotilere.service.HotelService;

import java.util.List;

public interface ServiceHotelRepository extends JpaRepository<ServiceHotel, Long> {
    List<ServiceHotel> findByHotelId(Long Hotelid);
}
