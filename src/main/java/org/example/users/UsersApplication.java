package org.example.users;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.annotations.OpenAPI31;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@OpenAPI31
@OpenAPIDefinition(
        info = @Info(
                title = "MedHeadPoc User",
                version = "1.0.0",
                description = "API for POC"
        ),
        servers = {
                @Server(url = "http://localhost:8082", description = "Local server"),
                @Server(url = "https://api.users.example.com", description = "Production server")
        }
)
public class UsersApplication {


    public static void main(String[] args) {
        SpringApplication.run(UsersApplication.class, args);
    }

}
