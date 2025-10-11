package tn.esprit.gestionhotilere.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.gestionhotilere.entity.Hotel;
import tn.esprit.gestionhotilere.entity.Role;
import tn.esprit.gestionhotilere.entity.ServiceHotel;
import tn.esprit.gestionhotilere.repository.HotelRepository;
import tn.esprit.gestionhotilere.repository.ServiceHotelRepository;

import java.util.List;

@Service

public class ServiceHotelService {
    @Autowired
    private ServiceHotelRepository serviceHotelRepository;
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private UserService userService;

    public ServiceHotel addService(Long hotelId, ServiceHotel service) {

           Long userId=userService.getOrCreateCurrentUser().getId();
           Hotel hotel=hotelRepository.findById(hotelId).get();

        if (!hotel.getAdmnistrateur().getId().equals(userId)
                && userService.getByIdkc(userId.toString()).getRole() != Role.SUPERADMIN) {
            throw new RuntimeException("Accès refusé");
        }
           service.setHotel(hotel);

          return serviceHotelRepository.save(service);

    }
    public List<ServiceHotel> getServicesByHotel(Long hotelId) {
        return serviceHotelRepository.findByHotelId(hotelId);
    }

    public void supprimerService(Long id) {
        serviceHotelRepository.deleteById(id);
    }


    public ServiceHotel updateService(Long hotelId, Long serviceId, ServiceHotel payload) {
        Long userId = userService.getOrCreateCurrentUser().getId();
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hôtel introuvable"));

        if (!hotel.getAdmnistrateur().getId().equals(userId)
                && userService.getByIdkc(userId.toString()).getRole() != Role.SUPERADMIN) {
            throw new RuntimeException("Accès refusé");
        }

        ServiceHotel service = serviceHotelRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service introuvable"));

        if (!service.getHotel().getId().equals(hotelId)) {
            throw new RuntimeException("Ce service n’appartient pas à cet hôtel");
        }

        service.setDescription(payload.getDescription());
        service.setPrix(payload.getPrix());
        service.setCapacite(payload.getCapacite());
        service.setDisponibilte(payload.getDisponibilte());

        return serviceHotelRepository.save(service);
    }

}

