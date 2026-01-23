package it.unisannio.gateway.config;

import it.unisannio.gateway.security.RoleFilter;
import jakarta.ws.rs.ApplicationPath;
import org.springframework.context.annotation.Configuration;
import org.glassfish.jersey.server.ResourceConfig;

@Configuration
@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig(RoleFilter roleFilter) {
        register(it.unisannio.gateway.presentation.PcForgeGatewayController.class);
        register(roleFilter);
    }

}