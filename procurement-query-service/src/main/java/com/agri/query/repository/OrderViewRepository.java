package com.agri.query.repository;

import com.agri.query.model.OrderView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderViewRepository extends MongoRepository<OrderView, String> {
    
    List<OrderView> findByBuyerId(String buyerId, Pageable pageable);
    
    List<OrderView> findBySupplierId(String supplierId, Pageable pageable);
    
    List<OrderView> findByStatus(String status, Pageable pageable);
    
    List<OrderView> findAllBy(Pageable pageable);
}
