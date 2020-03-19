package com.redroundrobin.thirema.apirest.service.postgres;

import java.util.ArrayList;
import java.util.List;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository repository;

    public List<User> findAll() {
        return (List<User>) repository.findAll();
    }

    public User find(int id){
        return repository.findById(id).get();
    }

    public User findByTelegramName(String telegramName){
        return repository.findByTelegramName(telegramName);
    }

    public User findByTelegramNameAndTelegramChat(String telegramName, String telegramChat){
        return repository.findByTelegramNameAndTelegramChat(telegramName, telegramChat);
    }

    public User findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = this.repository.findByEmail(s);
        if( user == null ) {
            throw new UsernameNotFoundException("");
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(),user.getPassword(), new ArrayList<>());
    }
}