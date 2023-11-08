package com.example.cashregister.services;

import com.example.cashregister.entities.Service;

import java.util.List;
import java.util.Optional;

public interface ServiceService {
    public List<Service> findAll();
    public Optional<Service> findById(int id);
    public Service save(Service service);
}
