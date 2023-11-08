package com.example.cashregister.services.impl;

import com.example.cashregister.repositories.ServiceRepository;
import com.example.cashregister.services.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceServiceImpl implements ServiceService {
    @Autowired
    private ServiceRepository serviceRepository;

    @Override
    public List<com.example.cashregister.entities.Service> findAll() {
        return serviceRepository.findAll();
    }

    @Override
    public Optional<com.example.cashregister.entities.Service> findById(int id) {
        return serviceRepository.findById(id);
    }

    @Override
    public com.example.cashregister.entities.Service save(com.example.cashregister.entities.Service service) {
        return serviceRepository.save(service);
    }
}
