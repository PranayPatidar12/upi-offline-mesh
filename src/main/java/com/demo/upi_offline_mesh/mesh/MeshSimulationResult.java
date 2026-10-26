package com.demo.upi_offline_mesh.mesh;
import com.demo.upi_offline_mesh.service.IngestionResult;
import lombok.Getter;
import java.util.List;

@Getter
public class MeshSimulationResult {
    public enum Status { DELIVERED, DROPPED_NO_ROUTE, DROPPED_TTL_EXPIRED }

    private final Status status;
    private final List<RelayHop> hops;
    private final IngestionResult ingestionResult;
    private final String droppedAtNodeId;

    private MeshSimulationResult(Status status, List<RelayHop> hops,
                                 IngestionResult ingestionResult, String droppedAtNodeId) {
        this.status = status;
        this.hops = hops;
        this.ingestionResult = ingestionResult;
        this.droppedAtNodeId = droppedAtNodeId;
    }

    public static MeshSimulationResult delivered(List<RelayHop> hops, IngestionResult ingestionResult) {
        return new MeshSimulationResult(Status.DELIVERED, hops, ingestionResult, null);
    }

    public static MeshSimulationResult droppedNoRoute(List<RelayHop> hops, String droppedAtNodeId) {
        return new MeshSimulationResult(Status.DROPPED_NO_ROUTE, hops, null, droppedAtNodeId);
    }

    public static MeshSimulationResult droppedTtlExpired(List<RelayHop> hops) {
        return new MeshSimulationResult(Status.DROPPED_TTL_EXPIRED, hops, null, null);
    }
}
