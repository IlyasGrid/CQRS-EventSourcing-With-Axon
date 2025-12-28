package com.ilyasgrid.eventsoucingwithaxon.query.service;

import com.ilyasgrid.eventsoucingwithaxon.common.queries.GetAccountByIdQuery;
import com.ilyasgrid.eventsoucingwithaxon.common.queries.GetAllAccountsQuery;
import com.ilyasgrid.eventsoucingwithaxon.query.entity.Account;
import com.ilyasgrid.eventsoucingwithaxon.query.repositories.AccountRepository;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountQueryHandler {
    private final AccountRepository repository;

    public AccountQueryHandler(AccountRepository repository) {
        this.repository = repository;
    }

    @QueryHandler
    public List<Account> handle(GetAllAccountsQuery query) {
        return repository.findAll();
    }

    @QueryHandler
    public Account handle(GetAccountByIdQuery query) {
        return repository.findById(query.id())
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

}
