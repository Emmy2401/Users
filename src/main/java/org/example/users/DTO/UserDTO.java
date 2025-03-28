package org.example.users.DTO;

import jakarta.validation.constraints.*;

public class UserDTO {
    private Long id;

    @NotBlank(message = "Username cannot be null or empty")
    private String username;

    private String role;

    public UserDTO() {
    }

    public UserDTO(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
