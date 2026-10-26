package com.demo.upi_offline_mesh.controller;

import com.demo.upi_offline_mesh.model.MeshPacket;
import com.demo.upi_offline_mesh.service.BridgeIngestionService;
import com.demo.upi_offline_mesh.service.IngestionResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {
    private final BridgeIngestionService bridgeIngestionService;

    public ApiController(BridgeIngestionService bridgeIngestionService){
        this.bridgeIngestionService = bridgeIngestionService;
    }

    @PostMapping("api/bridge/ingest")
    public IngestionResult ingest(@RequestBody MeshPacket packet){
        return bridgeIngestionService.ingest(packet);
    }
}
