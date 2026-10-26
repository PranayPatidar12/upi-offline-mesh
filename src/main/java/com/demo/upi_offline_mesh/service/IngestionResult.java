package com.demo.upi_offline_mesh.service;

public record IngestionResult(IngestionOutcome outcome, String packetHash, String reason, Long transactionId) {

    public static IngestionResult settled(String packetHash, Long transactionId){
        return new IngestionResult(IngestionOutcome.SETTLED, packetHash, null, transactionId );
    }

    public static IngestionResult duplicate(String packetHash){
        return new IngestionResult(IngestionOutcome.DUPLICATE_DROPPED, packetHash, null, null);
    }

    public static IngestionResult invalid(String packetHash, String reason){
        return new IngestionResult(IngestionOutcome.INVALID, packetHash, reason, null);
    }
}
