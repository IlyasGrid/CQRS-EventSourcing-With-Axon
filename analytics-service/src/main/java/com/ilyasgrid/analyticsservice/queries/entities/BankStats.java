package com.ilyasgrid.analyticsservice.queries.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankStats {

    @Id
    private String id;
    private double totalBalance;
    private int accountCount;
}
