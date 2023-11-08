package com.example.cashregister.services.impl;

import com.example.cashregister.entities.OrderInService;
import com.example.cashregister.repositories.OrderInServiceRepository;
import com.example.cashregister.services.OrderInServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderInServiceServiceImpl implements OrderInServiceService {
    @Autowired
    private OrderInServiceRepository orderInServiceRepository;
    @Override
    public OrderInService save(OrderInService orderInService) {
        return orderInServiceRepository.save(orderInService);
    }
}
