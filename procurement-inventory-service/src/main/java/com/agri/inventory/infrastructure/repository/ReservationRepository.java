package com.agri.inventory.infrastructure.repository;

import com.agri.inventory.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, String> {
    
    /**
     * Find all reservations for a specific order.
     */
    List<Reservation> findByOrderId(String orderId);
    
    /**
     * Find reservation by order ID and product ID for idempotency check.
     */
    Optional<Reservation> findByOrderIdAndProductId(String orderId, String productId);
}
