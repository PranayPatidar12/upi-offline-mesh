package com.demo.upi_offline_mesh.mesh;

import com.demo.upi_offline_mesh.model.MeshPacket;
import com.demo.upi_offline_mesh.service.BridgeIngestionService;
import com.demo.upi_offline_mesh.service.IngestionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;

    @Service
    @RequiredArgsConstructor
    public class MeshSimulatorService {

        private final MeshTopologyService topologyService;
        private final BridgeIngestionService bridgeIngestionService;

        public MeshSimulationResult relayPacket(String originNodeId, MeshPacket packet, int initialTtl) {
            if (!topologyService.isTopologyBuilt()) {
                throw new IllegalStateException("Mesh topology not built yet. Call buildRandomTopology first.");
            }

            List<RelayHop> hops = new ArrayList<>();
            String currentNodeId = originNodeId;
            int ttl = initialTtl;
            int hopNumber = 0;
            Set<String> visited = new HashSet<>();
            visited.add(currentNodeId);

            MeshNode gateway = topologyService.getGateway();

            while (!currentNodeId.equals(gateway.getNodeId()) && ttl > 0) {
                MeshNode currentNode = topologyService.getNode(currentNodeId);
                String nextNodeId = pickNextHop(currentNode, visited, gateway.getNodeId());

                if (nextNodeId == null) {
                    return MeshSimulationResult.droppedNoRoute(hops, currentNodeId);
                }

                hopNumber++;
                ttl--;
                hops.add(new RelayHop(currentNodeId, nextNodeId, hopNumber, ttl, Instant.now()));

                currentNodeId = nextNodeId;
                visited.add(currentNodeId);
            }

            if (!currentNodeId.equals(gateway.getNodeId())) {
                return MeshSimulationResult.droppedTtlExpired(hops);
            }

            IngestionResult ingestionResult = bridgeIngestionService.ingest(packet);
            return MeshSimulationResult.delivered(hops, ingestionResult);
        }

        private String pickNextHop(MeshNode currentNode, Set<String> visited, String gatewayId) {
            if (currentNode.getPeerIds().contains(gatewayId)) {
                return gatewayId;
            }

            List<String> unvisitedPeers = currentNode.getPeerIds().stream()
                    .filter(id -> !visited.contains(id))
                    .toList();

            if (unvisitedPeers.isEmpty()) {
                return null;
            }

            return unvisitedPeers.get(new Random().nextInt(unvisitedPeers.size()));
        }
    }

