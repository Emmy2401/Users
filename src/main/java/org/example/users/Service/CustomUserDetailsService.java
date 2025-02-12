package org.example.users.Service;

import org.example.users.DTO.SignupDTO;
import org.example.users.DTO.UserDTO;
import org.example.users.Entity.User;
import org.example.users.Exception.UserAlreadyExistsException;
import org.example.users.Exception.UserNotFoundException;
import org.example.users.Repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Collections;


public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found with username: " + username);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }

    public UserDTO getUserProfile(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        return new UserDTO(user.getId(), user.getUsername(), user.getRole());
    }

    public User registerUser(SignupDTO signupDTO) {
        if (userRepository.findByUsername(signupDTO.getUsername()) != null) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        User user = new User();
        user.setUsername(signupDTO.getUsername());
        user.setPassword(passwordEncoder.encode(signupDTO.getPassword())); // Hash the password
        user.setRole("ROLE_USER");

        return userRepository.save(user);
    }
}

