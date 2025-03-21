package org.example.users.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.users.DTO.LoginDTO;
import org.example.users.DTO.SignupDTO;
import org.example.users.DTO.UserDTO;
import org.example.users.Entity.User;
import org.example.users.Repository.UserRepository;
import org.example.users.Config.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur responsable de l'authentification des utilisateurs.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "USERSCONTROLLER", description = "Controller API USER")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Constructeur avec injection des dépendances.
     *
     * @param userRepository     Repository pour accéder aux utilisateurs.
     * @param passwordEncoder    Encodeur pour hacher les mots de passe.
     * @param jwtUtils           Outil de gestion des tokens JWT.
     * @param authenticationManager Gestionnaire d'authentification.
     */
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          JwtUtils jwtUtils, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Endpoint pour l'inscription des utilisateurs.
     *
     * @param signupDTO DTO contenant les informations d'inscription.
     * @return ResponseEntity avec le statut de la création.
     */
    @Operation(summary = "register", description = "enregistrer un utlisateur")
    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody SignupDTO signupDTO) {
        if (userRepository.findByUsername(signupDTO.getUsername()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Username already used"));
        }

        User user = new User();
        user.setUsername(signupDTO.getUsername());
        user.setPassword(passwordEncoder.encode(signupDTO.getPassword()));
        user.setRole("ROLE_USER");

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    /**
     * Endpoint pour la connexion et la génération d'un JWT.
     *
     * @param loginDTO DTO contenant le nom d'utilisateur et le mot de passe.
     * @return ResponseEntity avec le token JWT ou un message d'erreur.
     */
    @Operation(summary = "login", description = "loger un utilisateur")
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
            );

            if (authentication.isAuthenticated()) {
                String token = jwtUtils.generateToken(loginDTO.getUsername());
                return ResponseEntity.ok(Map.of("token", token, "type", "Bearer"));
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid username or password"));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Unexpected error: " + e.getMessage()));
        }
    }

    /**
     * Récupération du profil utilisateur authentifié.
     */
    @Operation(summary = "me", description = "consulter son profil")
    @GetMapping("/me")
    public ResponseEntity<Object> getProfile(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        User user = userRepository.findByUsername(authentication.getName());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
        UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(), user.getRole());

        return ResponseEntity.ok(userDTO);
    }
}
