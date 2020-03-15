package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Users;
import com.redroundrobin.thirema.apirest.repository.postgres.UsersRepository;
import com.redroundrobin.thirema.apirest.models.timescale.Sensors;
import com.redroundrobin.thirema.apirest.repository.timescale.SensorsTRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DBController {

    @Autowired
    private UsersRepository usersRepo;

    @Autowired
    private SensorsTRepository sensorsRepo;

    @RequestMapping(value = {"/showUsers"})
    public List<Users> findAccount() {

        var users = (List<Users>) usersRepo.findAll();

        return users;
    }

    @RequestMapping(value = {"/a"})
    public List<Sensors> findConditions() {
        var sensors = (List<Sensors>) sensorsRepo.findAll();

        return sensors;
    }
}