package it.unisannio.reviews;

import it.unisannio.reviews.presentation.TechnicianController;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReviewsApplication extends ResourceConfig {

    public ReviewsApplication() {
        register(TechnicianController.class);
    }
    public static void main(String[] args) {
        SpringApplication.run(ReviewsApplication.class, args);
    }

}