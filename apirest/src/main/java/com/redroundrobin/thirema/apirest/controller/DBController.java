package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.City;
import com.redroundrobin.thirema.apirest.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DBController {

    @Autowired
    private CityService cityService;

    @RequestMapping(value = {"/showCities"})
    public List<City> findCities(Model model) {

        var cities = (List<City>) cityService.findAll();

        return cities;
    }
}
