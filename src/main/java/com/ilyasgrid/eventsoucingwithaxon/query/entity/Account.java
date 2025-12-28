package com.ilyasgrid.eventsoucingwithaxon.query.entity;

import com.ilyasgrid.eventsoucingwithaxon.common.enums.AccountStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// In queries.entities
@Entity
@Data
@NoArgsConstructor @AllArgsConstructor
public class Account {
    @Id
    private String id;
    private double balance;
    private String currency;
    private AccountStatus status;
}
