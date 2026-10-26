package com.demo.upi_offline_mesh.mesh;

import java.time.Instant;

public record RelayHop(
        String fromNodeId,
        String toNodeId,
        int hopNumber,
        int remainingTtl,
        Instant timestamp
) {}
