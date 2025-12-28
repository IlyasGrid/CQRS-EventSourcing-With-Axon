package com.ilyasgrid.eventsoucingwithaxon.query.service;

import com.ilyasgrid.eventsoucingwithaxon.common.events.AccountCreatedEvent;
import com.ilyasgrid.eventsoucingwithaxon.common.events.AccountCreditedEvent;
import com.ilyasgrid.eventsoucingwithaxon.common.events.DebitedAccountEvent;
import com.ilyasgrid.eventsoucingwithaxon.common.queries.GetAllAccountsQuery;
import com.ilyasgrid.eventsoucingwithaxon.query.entity.Account;
import com.ilyasgrid.eventsoucingwithaxon.query.repositories.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

// In queries.service
@Component
@Slf4j
public class AccountServiceHandler {

    private final AccountRepository accountRepository;

    public AccountServiceHandler(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // This method reacts to the "Fact" stored in the Event Store
    @EventHandler
    public void on(AccountCreatedEvent event) {
        log.info("AccountCreatedEvent received: updating Read Model");

        Account account = new Account(
                event.accountId(),
                event.initialBalance(),
                event.currency(),
                event.accountStatus()
        );

        // We save the "Current State" to our SQL database
        accountRepository.save(account);
    }
    @EventHandler
    public void on(AccountCreditedEvent event) {
        Account account = accountRepository.findById(event.id())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setBalance(account.getBalance() + event.amount());
        accountRepository.save(account);
    }


    @EventHandler
    public void on(DebitedAccountEvent event) {
        Account account = accountRepository.findById(event.id())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setBalance(account.getBalance() - event.amount());
        accountRepository.save(account);
    }
}