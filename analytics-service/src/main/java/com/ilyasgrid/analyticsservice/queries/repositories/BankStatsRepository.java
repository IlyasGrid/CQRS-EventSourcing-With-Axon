package com.ilyasgrid.analyticsservice.queries.repositories;

import com.ilyasgrid.analyticsservice.queries.entities.BankStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankStatsRepository extends JpaRepository<BankStats, String> {
}
