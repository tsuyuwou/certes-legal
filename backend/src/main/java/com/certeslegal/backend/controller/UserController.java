package com.certeslegal.backend.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.certeslegal.backend.exception.AccountAlreadyExistsException;
import com.certeslegal.backend.exception.InvalidCredentialsException;
import com.certeslegal.backend.model.Job;
import com.certeslegal.backend.model.User;
import com.certeslegal.backend.repository.JobRepository;
import com.certeslegal.backend.repository.UserRepository;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1/")
public class UserController {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    @Autowired
    public UserController(UserRepository userRepository, JobRepository jobRepository) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
    }

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleException(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<String> handleException(AccountAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    
    // authenticate user
    @PostMapping("/user/auth")
    public ResponseEntity<Map<String, Object>> authenticate(@RequestBody User user) {
        User existingUser = userRepository.findByEmail(user.getEmail());

        // log in
        if (user.getFirstName() == null) {
            if (existingUser == null || !passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
                throw new InvalidCredentialsException("Invalid email or password.");
            }
            else {
                existingUser.setPassword(user.getPassword());
                Map<String, Object> response = new HashMap<>();
                response.put("appliedJobs", existingUser.getJobs().stream().map(Job::getId).collect(Collectors.toSet()));
                existingUser.setJobs(null);
                response.put("user", existingUser);
                return ResponseEntity.ok(response);
            }
        }

        // register
        else {
            if (existingUser != null) {
                throw new AccountAlreadyExistsException("An account with that email already exists.");
            }
            else {
                String password = user.getPassword();
                user.setPassword(passwordEncoder.encode(password));
                user = userRepository.save(user);
                user.setPassword(password);
                Map<String, Object> response = new HashMap<>();
                response.put("user", user);
                response.put("appliedJobs", new Integer[0]);
                return ResponseEntity.ok(response);
            }
        }
    }

    // apply to a job
    @PostMapping("/user/{userId}/job/{jobId}")
    @ResponseStatus(HttpStatus.OK)
    public Set<Integer> applyToJob(@PathVariable Integer userId, @PathVariable Integer jobId) {
        User user = userRepository.getReferenceById(userId);
        user.getJobs().add(jobRepository.getReferenceById(jobId));
        userRepository.save(user);
        return user.getJobs().stream().map(Job::getId).collect(Collectors.toSet());
    }

    // withdraw a job application
    @DeleteMapping("/user/{userId}/job/{jobId}")
    @ResponseStatus(HttpStatus.OK)
    public Set<Integer> withdrawApplication(@PathVariable Integer userId, @PathVariable Integer jobId) {
        User user = userRepository.getReferenceById(userId);
        user.getJobs().remove(jobRepository.getReferenceById(jobId));
        userRepository.save(user);
        return user.getJobs().stream().map(Job::getId).collect(Collectors.toSet());
    }

    // update account
    @PatchMapping("/user/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable Integer id, @RequestBody Map<String, String> data) {
        User user = userRepository.getReferenceById(id);
        if (data.containsKey("firstName")) {
            user.setFirstName(data.get("firstName"));
        }
        else if (data.containsKey("lastName")) {
            user.setLastName(data.get("lastName"));
        }
        else if (data.containsKey("email")) {
            String email = data.get("email");
            if (userRepository.findByEmail(email) == null) {
                user.setEmail(email);
            }
            else {
                throw new AccountAlreadyExistsException("An account associated with that email address already exists.");
            }
        }
        else {
            user.setPassword(passwordEncoder.encode(data.get("password")));
        }
        user = userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    // delete account
    @DeleteMapping("/user/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteAccount(@PathVariable Integer id) {
        userRepository.deleteById(id);
    }
}
