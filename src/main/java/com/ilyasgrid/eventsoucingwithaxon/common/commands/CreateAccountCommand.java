package com.ilyasgrid.eventsoucingwithaxon.common.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.axonframework.modelling.command.TargetAggregateIdentifier;


public record CreateAccountCommand(
        @TargetAggregateIdentifier
        String accountId,
        double initialBalance,
        String currency
) {}
