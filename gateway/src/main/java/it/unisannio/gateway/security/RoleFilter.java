package it.unisannio.gateway.security;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Component
@Provider
@HasRole({}) // placeholder
@Priority(Priorities.AUTHORIZATION)
public class RoleFilter implements ContainerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public RoleFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        // Recupera l’Authorization header
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Missing or Invalid Token").build());
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid Token").build());
            return;
        }

        // Ottieni ruoli dal token
        String rolesStr = jwtTokenProvider.getRoles(token);
        List<String> userRoles = Arrays.asList(rolesStr.split(","));

        // Recupera i ruoli richiesti dall’annotazione @HasRole sul metodo
        Method method = requestContext.getProperty("jersey.config.server.resource.method") != null
                ? (Method) requestContext.getProperty("jersey.config.server.resource.method")
                : null;

        if (method != null && method.isAnnotationPresent(HasRole.class)) {
            String[] requiredRoles = method.getAnnotation(HasRole.class).value();
            boolean allowed = Arrays.stream(requiredRoles)
                    .anyMatch(userRoles::contains);

            if (!allowed) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                        .entity("Access denied: must have one role " + String.join(", ", requiredRoles))
                        .build());
            }
        }
    }

}