package com.demo.upi_offline_mesh.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IdempotencyService {
    private final ConcurrentHashMap<String, Instant> seen = new ConcurrentHashMap<>();

    public boolean claim(String packetHash){
        Instant previous =  seen.putIfAbsent(packetHash, Instant.now());
        return previous==null;
    }

    public int size(){
        return seen.size();
    }

    public void clear(){
        seen.clear();
    }

}
