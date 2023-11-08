package com.example.cashregister.services.impl;

import com.example.cashregister.entities.Order;
import com.example.cashregister.repositories.OrderRepository;
import com.example.cashregister.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Optional<Order> findById(int id) {
        return orderRepository.findById(id);
    }
}
