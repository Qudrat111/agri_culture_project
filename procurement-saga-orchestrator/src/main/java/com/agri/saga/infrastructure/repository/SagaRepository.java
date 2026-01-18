package com.agri.saga.infrastructure.repository;

import com.agri.saga.domain.ProcurementSaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SagaRepository extends JpaRepository<ProcurementSaga, String> {
    
    Optional<ProcurementSaga> findByOrderId(String orderId);
}
