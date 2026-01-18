package com.agri.order.infrastructure.repository;

import com.agri.order.domain.ProcurementOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<ProcurementOrder, String> {
}
