package com.ilyasgrid.eventsoucingwithaxon.command.controllers;

import com.ilyasgrid.eventsoucingwithaxon.common.commands.CreateAccountCommand;
import com.ilyasgrid.eventsoucingwithaxon.common.commands.CreditAccountCommand;
import com.ilyasgrid.eventsoucingwithaxon.common.commands.DebitedAccountCommand;
import com.ilyasgrid.eventsoucingwithaxon.common.dtos.*;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/commands/accounts")
public class AccountCommandController {
    private final CommandGateway commandGateway;

    public AccountCommandController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @PostMapping("/create")
    public CompletableFuture<String> createAccount(@RequestBody CreateAccountDTO createAccountCommand) {
        CompletableFuture<String> response = commandGateway.send(new CreateAccountCommand(UUID.randomUUID().toString(), createAccountCommand.initialBalance(), createAccountCommand.currency()));
        return response;
    }

    @PutMapping("/credit")
    public CompletableFuture<String> creditAccount(@RequestBody CreditAccountDTO request) {
        return commandGateway.send(new CreditAccountCommand(
                request.id(),
                request.amount(),
                request.currency()
        ));
    }
    @PutMapping("/debit")
    public CompletableFuture<String> debitAccount(@RequestBody DebitedAccountDTO request) {
        return commandGateway.send(new DebitedAccountCommand(
                request.id(),
                request.amount(),
                request.currency()
        ));
    }

    @ExceptionHandler(Exception.class)
    public String ExceptionHandler(Exception e) {
        return e.getMessage();
    }
}
