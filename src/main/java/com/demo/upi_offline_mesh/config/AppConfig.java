package com.demo.upi_offline_mesh.config;


import com.demo.upi_offline_mesh.model.Account;
import com.demo.upi_offline_mesh.model.AccountRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.math.BigDecimal;

@Configuration
@EnableScheduling
public class AppConfig {

    @Bean
    CommandLineRunner seedAccounts(AccountRepo accountRepo){
        return args ->{
            if (accountRepo.count() == 0) {
                accountRepo.save(new Account("alice@upi", "Alice", new BigDecimal("1000.00")));
                accountRepo.save(new Account("bob@upi", "Bob", new BigDecimal("500.00")));
                accountRepo.save(new Account("carol@upi", "Carol", new BigDecimal("250.00")));
            }

        };
    }

}
