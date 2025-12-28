package com.ilyasgrid.eventsoucingwithaxon.common.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record DebitedAccountCommand (
        @TargetAggregateIdentifier
        String id,
        double amount,
        String currency
) {
}
