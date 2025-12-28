package com.ilyasgrid.eventsoucingwithaxon.query.controllers;

import com.ilyasgrid.eventsoucingwithaxon.common.queries.GetAccountByIdQuery;
import com.ilyasgrid.eventsoucingwithaxon.common.queries.GetAllAccountsQuery;
import com.ilyasgrid.eventsoucingwithaxon.query.entity.Account;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

// In queries.controllers
@RestController
@RequestMapping("/queries/account")
public class AccountQueryController {

    private final QueryGateway queryGateway;

    public AccountQueryController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @GetMapping("/all")
    public CompletableFuture<List<Account>> getAllAccounts() {
        return queryGateway.query(
                new GetAllAccountsQuery(),
                ResponseTypes.multipleInstancesOf(Account.class)
        );
    }

    @GetMapping("/{id}")
    public CompletableFuture<Account> getAccount(@PathVariable String id) {
        return queryGateway.query(new GetAccountByIdQuery(id),
                ResponseTypes.instanceOf(Account.class));
    }
}
