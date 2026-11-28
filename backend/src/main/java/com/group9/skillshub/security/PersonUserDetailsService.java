package com.group9.skillshub.security;

import java.util.List;

import com.group9.skillshub.organizations.model.Person;
import com.group9.skillshub.organizations.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Logs in by person id; everyone shares the demo password since the sponsor data has no credentials. */
@Service
public class PersonUserDetailsService implements UserDetailsService {

    private final PersonRepository personRepository;
    private final String encodedDemoPassword;

    public PersonUserDetailsService(
            PersonRepository personRepository,
            PasswordEncoder passwordEncoder,
            @Value("${auth.demo-password:demo123}") String demoPassword) {
        this.personRepository = personRepository;
        this.encodedDemoPassword = passwordEncoder.encode(demoPassword);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        int personId;
        try {
            personId = Integer.parseInt(username);
        } catch (NumberFormatException exception) {
            throw new UsernameNotFoundException("Unknown person " + username);
        }
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown person " + username));
        String authority = person.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER";
        return User.withUsername(username)
                .password(encodedDemoPassword)
                .authorities(List.of(new SimpleGrantedAuthority(authority)))
                .build();
    }
}
