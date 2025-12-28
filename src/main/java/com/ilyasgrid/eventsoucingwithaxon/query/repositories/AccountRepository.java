package com.ilyasgrid.eventsoucingwithaxon.query.repositories;

import com.ilyasgrid.eventsoucingwithaxon.query.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
}