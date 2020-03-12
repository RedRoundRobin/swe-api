package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Account;
import com.redroundrobin.thirema.apirest.models.timescale.Conditions;
import com.redroundrobin.thirema.apirest.models.timescale.Conditions1;
import com.redroundrobin.thirema.apirest.repository.postgres.AccountRepository;
import com.redroundrobin.thirema.apirest.repository.timescale.ConditionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DBController {

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private ConditionsRepository conditionsRepo;

    @RequestMapping(value = {"/showUsers"})
    public List<Account> findAccount() {

        var users = (List<Account>) accountRepo.findAll();

        return users;
    }

    @RequestMapping(value = {"/showConditions"})
    public List<Conditions> findConditions() {
        Conditions1 c = new Conditions1();

        return c.prova();
    }
}