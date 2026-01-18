package com.agri.inventory.infrastructure.repository;

import com.agri.inventory.domain.InventoryItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, String> {
    
    /**
     * Find inventory item by product ID with pessimistic write lock.
     * This prevents concurrent modifications and ensures consistency during reservations.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryItem i WHERE i.productId = :productId")
    Optional<InventoryItem> findByProductIdWithLock(@Param("productId") String productId);
    
    /**
     * Find inventory item by product ID without locking.
     */
    Optional<InventoryItem> findByProductId(String productId);
}
