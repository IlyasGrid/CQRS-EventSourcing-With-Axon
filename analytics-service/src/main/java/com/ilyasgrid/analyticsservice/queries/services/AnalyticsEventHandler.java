package com.ilyasgrid.analyticsservice.queries.services;

import com.ilyasgrid.analyticsservice.queries.entities.BankStats;
import com.ilyasgrid.analyticsservice.queries.repositories.BankStatsRepository;
import com.ilyasgrid.analyticsservice.common_api.events.AccountCreatedEvent;
import com.ilyasgrid.analyticsservice.common_api.events.AccountCreditedEvent;
import com.ilyasgrid.analyticsservice.common_api.events.AccountDebitedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AnalyticsEventHandler {

    private static final String STATS_ID = "BANK_STATS_001";
    private final BankStatsRepository bankStatsRepository;

    @EventHandler
    public void on(AccountCreatedEvent event) {
        log.info("Handling AccountCreatedEvent for account: {}", event.accountId());

        BankStats stats = bankStatsRepository.findById(STATS_ID)
                .orElse(new BankStats(STATS_ID, 0.0, 0));

        stats.setAccountCount(stats.getAccountCount() + 1);
        stats.setTotalBalance(stats.getTotalBalance() + event.initialBalance());

        bankStatsRepository.save(stats);
        log.info("Updated stats - Total Balance: {}, Account Count: {}",
                stats.getTotalBalance(), stats.getAccountCount());
    }

    @EventHandler
    public void on(AccountCreditedEvent event) {
        log.info("Handling AccountCreditedEvent for account: {}", event.id());

        BankStats stats = bankStatsRepository.findById(STATS_ID)
                .orElse(new BankStats(STATS_ID, 0.0, 0));

        stats.setTotalBalance(stats.getTotalBalance() + event.amount());

        bankStatsRepository.save(stats);
        log.info("Updated stats after credit - Total Balance: {}", stats.getTotalBalance());
    }

    @EventHandler
    public void on(AccountDebitedEvent event) {
        log.info("Handling AccountDebitedEvent for account: {}", event.id());

        BankStats stats = bankStatsRepository.findById(STATS_ID)
                .orElse(new BankStats(STATS_ID, 0.0, 0));

        stats.setTotalBalance(stats.getTotalBalance() - event.amount());

        bankStatsRepository.save(stats);
        log.info("Updated stats after debit - Total Balance: {}", stats.getTotalBalance());
    }
}
