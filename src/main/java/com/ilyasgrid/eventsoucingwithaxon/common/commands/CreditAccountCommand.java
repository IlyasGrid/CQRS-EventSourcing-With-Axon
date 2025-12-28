package com.ilyasgrid.eventsoucingwithaxon.common.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record CreditAccountCommand(
        @TargetAggregateIdentifier
        String id,
        double amount,
        String currency

) {
}
