package it.unisannio.saldotecnico;

import it.unisannio.saldotecnico.presentation.TechnicianController;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SaldoTecnicoApplication extends ResourceConfig {

    public SaldoTecnicoApplication() {
        register(TechnicianController.class);
    }
    public static void main(String[] args) {
        SpringApplication.run(SaldoTecnicoApplication.class, args);
    }

}