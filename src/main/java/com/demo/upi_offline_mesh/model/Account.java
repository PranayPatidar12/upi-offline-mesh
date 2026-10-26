package com.demo.upi_offline_mesh.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Entity
@Table(name = "accounts")
@Getter
@Setter@NoArgsConstructor

public class Account {
    @Id
    private String vpa;
 private String ownerName;
 private BigDecimal balance;

 @Version
    private Long version;

    public Account(String vpa, String ownerName, BigDecimal balance){
        this.vpa = vpa;
        this.ownerName = ownerName;
        this.balance = balance;
    }
}
