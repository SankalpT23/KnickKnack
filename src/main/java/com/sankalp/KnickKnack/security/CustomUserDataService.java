package com.sankalp.KnickKnack.security;

import com.sankalp.KnickKnack.model.User;
import com.sankalp.KnickKnack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;

//Spring Security needs to know how to look up a user during the authentication process
@Slf4j
@Service
public class CustomUserDataService implements UserDetailsService {

    @Autowired
    private UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Loading user by email: {}", email);
        User user = repository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User Not Found with email: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(Collections.emptyList())
                .disabled(!user.getIsActive())
                .build();
    }
}
