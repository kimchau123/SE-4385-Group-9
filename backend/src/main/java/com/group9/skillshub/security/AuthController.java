package com.group9.skillshub.security;

import com.group9.skillshub.common.ResourceNotFoundException;
import com.group9.skillshub.organizations.model.Person;
import com.group9.skillshub.organizations.repository.PersonRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final PersonRepository personRepository;

    public AuthController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        int personId = Integer.parseInt(authentication.getName());
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new ResourceNotFoundException("Person " + personId + " was not found"));
        return ResponseEntity.ok(new CurrentUserResponse(
                person.getPersonId(), person.getPersonName(), person.getRole(), person.isAdmin()));
    }
}