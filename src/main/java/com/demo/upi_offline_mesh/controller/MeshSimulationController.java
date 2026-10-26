package com.demo.upi_offline_mesh.controller;
import com.demo.upi_offline_mesh.crypto.HybridCryptoService;
import com.demo.upi_offline_mesh.mesh.MeshNode;
import com.demo.upi_offline_mesh.mesh.MeshSimulationResult;
import com.demo.upi_offline_mesh.mesh.MeshSimulatorService;
import com.demo.upi_offline_mesh.mesh.MeshTopologyService;
import com.demo.upi_offline_mesh.model.MeshPacket;
import com.demo.upi_offline_mesh.model.PaymentInstruction;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mesh")
public class MeshSimulationController {
    private final MeshTopologyService topologyService;
    private final MeshSimulatorService simulatorService;
    private final HybridCryptoService cryptoService;

    public MeshSimulationController(MeshTopologyService topologyService,
                                    MeshSimulatorService simulatorService,
                                    HybridCryptoService cryptoService) {
        this.topologyService = topologyService;
        this.simulatorService = simulatorService;
        this.cryptoService = cryptoService;
    }

    @PostMapping("/topology")
    public String buildTopology(@RequestParam(defaultValue = "10") int deviceCount,
                                @RequestParam(defaultValue = "3") int avgConnections) {
        topologyService.buildRandomTopology(deviceCount, avgConnections);
        return "Topology built: " + deviceCount + " devices, avg " + avgConnections + " connections each";
    }

    @PostMapping("/simulate")
    public MeshSimulationResult simulate(@RequestParam String originNodeId,
                                         @RequestParam(defaultValue = "6") int ttl,
                                         @RequestParam String sender,
                                         @RequestParam String receiver,
                                         @RequestParam BigDecimal amount) throws Exception {

        PaymentInstruction instruction = new PaymentInstruction(sender, receiver, amount, "dummyPinHash",
                UUID.randomUUID().toString(), System.currentTimeMillis());

        String cipherText = cryptoService.encrypt(instruction);
        MeshPacket packet = new MeshPacket(UUID.randomUUID().toString(), 5, System.currentTimeMillis(), cipherText);

        return simulatorService.relayPacket(originNodeId, packet, ttl);


    }

    @GetMapping("/nodes")
    public List<String> listNodes() {
        return topologyService.getAllNodes()
                .stream()
                .map(MeshNode::getNodeId)
                .toList();
    }
}

