package it.unisannio.authorization;

import it.unisannio.authorization.presentation.UserController;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ApplicationPath("/login")
public class AuthorizationApplication extends ResourceConfig {

    public AuthorizationApplication() {
        register(UserController.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(AuthorizationApplication.class, args);
    }

}