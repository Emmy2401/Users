package org.example.users;

import org.example.users.Controller.UserController;
import org.example.users.DTO.LoginDTO;
import org.example.users.DTO.SignupDTO;
import org.example.users.DTO.UserDTO;
import org.example.users.Entity.User;
import org.example.users.Repository.UserRepository;
import org.example.users.Config.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private SignupDTO signupDTO;
    private LoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("hashedPassword");
        testUser.setRole("ROLE_USER");

        signupDTO = new SignupDTO();
        signupDTO.setUsername("testuser");
        signupDTO.setPassword("password123");

        loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password123");
    }

    @Test
    void register_Success() {
        when(userRepository.findByUsername(signupDTO.getUsername())).thenReturn(null);
        when(passwordEncoder.encode(signupDTO.getPassword())).thenReturn("hashedPassword");

        ResponseEntity<Object> response = userController.register(signupDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("message"));
        assertEquals("User registered successfully", ((Map<?, ?>) response.getBody()).get("message"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_UsernameAlreadyExists() {
        when(userRepository.findByUsername(signupDTO.getUsername())).thenReturn(testUser);

        ResponseEntity<Object> response = userController.register(signupDTO);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
        assertEquals("Username already used", ((Map<?, ?>) response.getBody()).get("error"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(jwtUtils.generateToken(loginDTO.getUsername())).thenReturn("fakeToken");

        ResponseEntity<Object> response = userController.login(loginDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("fakeToken", responseBody.get("token"));
        assertEquals("Bearer", responseBody.get("type"));
    }

    @Test
    void login_InvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        ResponseEntity<Object> response = userController.login(loginDTO);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
        assertEquals("Invalid username or password", ((Map<?, ?>) response.getBody()).get("error"));
    }

    @Test
    void getProfile_Success() {
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);

        ResponseEntity<Object> response = userController.getProfile(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserDTO userDTO = (UserDTO) response.getBody();
        assertNotNull(userDTO);
        assertEquals(testUser.getUsername(), userDTO.getUsername());
        assertEquals(testUser.getRole(), userDTO.getRole());
    }

    @Test
    void getProfile_Unauthorized() {
        ResponseEntity<Object> response = userController.getProfile(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
        assertEquals("Unauthorized", ((Map<?, ?>) response.getBody()).get("error"));
    }

    @Test
    void getProfile_UserNotFound() {
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(null);

        ResponseEntity<Object> response = userController.getProfile(authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
        assertEquals("User not found", ((Map<?, ?>) response.getBody()).get("error"));
    }
}