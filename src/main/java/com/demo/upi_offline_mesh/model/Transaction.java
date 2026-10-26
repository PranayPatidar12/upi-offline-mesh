package com.demo.upi_offline_mesh.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions", uniqueConstraints = {
        @UniqueConstraint(columnNames = "packetHash")

})
@Getter
@Setter
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderVpa;
    private String receiverVpa;
    BigDecimal amount;
    private String packetHash;
    private Instant settledAt;

    public Transaction(String senderVpa, String receiverVpa, BigDecimal amount, String packetHash, Instant settledAt) {
        this.senderVpa = senderVpa;
        this.receiverVpa = receiverVpa;
        this.amount = amount;
        this.packetHash = packetHash;
        this.settledAt = settledAt;
    }

}
