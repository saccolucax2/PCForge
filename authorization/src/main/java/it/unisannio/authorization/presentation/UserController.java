package it.unisannio.authorization.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unisannio.authorization.application.UserService;
import it.unisannio.authorization.data.Roles;
import it.unisannio.authorization.data.User;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.net.URI;
import java.util.List;
import java.util.Set;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @POST
    @Path("/authenticate")
    public Response authenticateUserJson(User user) {
        try {
            boolean authenticated = userService.authenticateUser(user.getUsername(), user.getPassword());
            if (!authenticated) {
                return Response.status(Response.Status.UNAUTHORIZED).build(); // 401
            }

            List<String> roles = userService.getUserRolesAsString(user.getUsername());

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode responseJson = mapper.createObjectNode();
            responseJson.put("username", user.getUsername());

            ArrayNode rolesArray = mapper.createArrayNode();
            for (String role : roles) {
                rolesArray.add(role);
            }
            responseJson.set("roles", rolesArray);

            return Response.ok(responseJson.toString(), MediaType.APPLICATION_JSON).build();

        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @POST
    public Response createUser(User user, @Context UriInfo uriInfo) {
        try {
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                user.setRoles(Set.of(Roles.USER));
            }

            if (user.getBirthDate() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Date of Birth required (formato: dd/MM/yyyy).")
                        .build();
            }

            String username = userService.createUser(user);
            URI uri = UriBuilder.fromUri(uriInfo.getAbsolutePath()).path(username).build();
            return Response.created(uri).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("Internal error: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{username}")
    public Response getUser(@PathParam("username") String username) {
        User user = userService.getUser(username);
        if (user != null) {
            return Response.ok(user).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/all")
    public Response getAllUsers() {
        List<User> users = userService.getAllUsers();
        if (users != null && !users.isEmpty()) {
            return Response.ok(users).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/role/{role}")
    public Response getUsersByRole(@PathParam("role") String role) {
        List<User> users= userService.findAllByRole(role);
            return Response.ok(users).build();
    }

    /**
     * Aggiunge l'ID di una build al set dell'utente.
     */
    @POST
    @Path("/{username}/builds/{buildId}")
    public Response addBuildToUser(@PathParam("username") String username,
                                   @PathParam("buildId") Long buildId) {
        try {
            userService.addGeneratedBuild(username, buildId);
            return Response.ok()
                    .entity("Build correctly added to user.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    /**
     * Rimuove l'ID di una build dal set dell'utente.
     */
    @DELETE
    @Path("/{username}/builds/{buildId}")
    public Response removeBuildFromUser(@PathParam("username") String username,
                                        @PathParam("buildId") Long buildId) {
        try {
            userService.removeGeneratedBuild(username, buildId);
            return Response.ok()
                    .entity("Build correctly removed from user.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    /**
     * Restituisce tutte le build generate dall'utente.
     */
    @GET
    @Path("/{username}/builds")
    public Response getUserBuilds(@PathParam("username") String username) {
        try {
            Set<Long> builds = userService.getGeneratedBuilds(username);
            return Response.ok(builds).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/{username}")
    public Response updateUser(@PathParam("username") String username, User user) {
        try {
            User updatedUser = userService.updateUser(username, user);
            return Response.ok(updatedUser).build();
        } catch (IllegalArgumentException e) {
            // Password non valida o altri errori di input
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("Internal Error: " + e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/{username}")
    public Response deleteUser(@PathParam("username") String username) {
        try {
            boolean deleted = userService.deleteUser(username);
            if (deleted) {
                return Response.noContent().build(); // 204
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

}