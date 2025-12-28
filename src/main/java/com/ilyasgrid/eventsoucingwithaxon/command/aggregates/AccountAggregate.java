package com.ilyasgrid.eventsoucingwithaxon.command.aggregates;

import com.ilyasgrid.eventsoucingwithaxon.common.commands.CreateAccountCommand;
import com.ilyasgrid.eventsoucingwithaxon.common.commands.CreditAccountCommand;
import com.ilyasgrid.eventsoucingwithaxon.common.commands.DebitedAccountCommand;
import com.ilyasgrid.eventsoucingwithaxon.common.enums.AccountStatus;
import com.ilyasgrid.eventsoucingwithaxon.common.events.AccountCreatedEvent;
import com.ilyasgrid.eventsoucingwithaxon.common.events.AccountCreditedEvent;
import com.ilyasgrid.eventsoucingwithaxon.common.events.DebitedAccountEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
@Slf4j
public class AccountAggregate {

    @AggregateIdentifier
    private String accountId;
    private double balance;
    private String currency;
    private AccountStatus accountStatus;

    public AccountAggregate() {
    }

    @CommandHandler
    public AccountAggregate(CreateAccountCommand command) {
        if (command.initialBalance() <= 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        AggregateLifecycle.apply(new AccountCreatedEvent(command.accountId(), command.initialBalance(), command.currency(), AccountStatus.ACTIVE));
    }

    @EventSourcingHandler
    public void on(AccountCreatedEvent event) {
        log.info("Account.java created: {}", event.accountId());
        this.accountId = event.accountId();
        this.balance = event.initialBalance();
        this.currency = event.currency();
        this.accountStatus = event.accountStatus();
    }


    @CommandHandler
    public void handle(CreditAccountCommand command) {
        if (command.amount() <= 0) {
            throw new RuntimeException("Amount must be positive");
        }
        AggregateLifecycle.apply(new AccountCreditedEvent(
                command.id(),
                command.amount(),
                command.currency()
        ));
    }

    @EventSourcingHandler
    public void on(AccountCreditedEvent event) {
        this.balance += event.amount();
    }

    @CommandHandler
    public void handle(DebitedAccountCommand command) {
        if (command.amount() <= 0) {
            throw new RuntimeException("Amount must be positive");
        }
        if (this.balance < command.amount()) {
            throw new RuntimeException("Insufficient balance");
        }
        AggregateLifecycle.apply(new DebitedAccountEvent(
                command.id(),
                command.amount(),
                command.currency()
        ));
    }

    @EventSourcingHandler
    public void on(DebitedAccountEvent event) {
        this.balance -= event.amount();
    }
}