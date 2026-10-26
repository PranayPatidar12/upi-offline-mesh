package com.demo.upi_offline_mesh.service;

public record SettlementResult(SettlementOutcome outcome, Long TransactionId) {

    public static SettlementResult success(Long Transactionid){
        return new SettlementResult(SettlementOutcome.SUCCESS, Transactionid);
    }

    public static  SettlementResult failed(SettlementOutcome outcome){
        return new SettlementResult(outcome, null );
    }

}
