package com.example.cashregister.services;

import com.example.cashregister.entities.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    public List<Order> findAll();
    public Order save(Order order);
    public Optional<Order> findById(int id);
}
