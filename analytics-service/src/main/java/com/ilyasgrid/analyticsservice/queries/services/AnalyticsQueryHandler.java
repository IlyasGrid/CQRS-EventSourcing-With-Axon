package com.ilyasgrid.analyticsservice.queries.services;

import com.ilyasgrid.analyticsservice.common_api.queries.GetBankStatsQuery;
import com.ilyasgrid.analyticsservice.queries.entities.BankStats;
import com.ilyasgrid.analyticsservice.queries.repositories.BankStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsQueryHandler {

    private static final String STATS_ID = "BANK_STATS_001";
    private final BankStatsRepository bankStatsRepository;

    @QueryHandler
    public BankStats handle(GetBankStatsQuery query) {
        log.info("Handling GetBankStatsQuery");
        return bankStatsRepository.findById(STATS_ID)
                .orElse(new BankStats(STATS_ID, 0.0, 0));
    }
}
