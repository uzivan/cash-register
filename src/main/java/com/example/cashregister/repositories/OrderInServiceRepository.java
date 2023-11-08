package com.example.cashregister.repositories;

import com.example.cashregister.entities.OrderInService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderInServiceRepository extends JpaRepository<OrderInService, Integer> {
}
