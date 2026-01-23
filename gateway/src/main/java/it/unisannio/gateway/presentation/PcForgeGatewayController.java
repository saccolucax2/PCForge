package it.unisannio.gateway.presentation;

import it.unisannio.gateway.security.HasRole;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unisannio.gateway.security.JwtTokenProvider;
import jakarta.ws.rs.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import java.util.*;
import static jakarta.ws.rs.client.ClientBuilder.newClient;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.stream.Collectors;

@Component
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/login")
//@RolesAllowed({"USER", "ADMIN"})
public class PcForgeGatewayController {

    @Value("${authorization.path}")
    private String authorizationServiceUrl;
    @Value("${chat.path}")
    private String chatServiceUrl;

    @Value("${buildgenerator.path}")
    private String buildServiceUrl;

    @Value("${comment.path}") // esempio: http://comment-service:8082/comments
    private String commentServiceUrl;

    @Value("${post.path}")
    private String postServiceUrl;

    @Value("${like.path}")
    private String likeServiceUrl;

    @Value("${saldotecnico.path}")
    private String saldoTecnicoServiceUrl;

    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public PcForgeGatewayController(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @POST
    @Path("/register")
    public Response createUser(JsonNode user, @Context UriInfo uriInfo) {
        String URL = authorizationServiceUrl;
        Client client = newClient();
        return client.target(URL)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(user, MediaType.APPLICATION_JSON));
    }

    @POST
    public Response login(JsonNode credentials) {
        try {
            String username = credentials.get("username").asText();
            String password = credentials.get("password").asText();

            Client client = ClientBuilder.newClient();
            Response authResponse = client.target(authorizationServiceUrl + "/authenticate")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(credentials));

            if (authResponse.getStatus() != 200) {
                return Response.status(401)
                        .entity("{\"message\": \"Invalid username or password\"}")
                        .build();
            }

            String authJson = authResponse.readEntity(String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode authNode = mapper.readTree(authJson);

            String authUsername = authNode.get("username").asText();
            List<String> roles = new ArrayList<>();
            if (authNode.has("roles")) {
                for (JsonNode roleNode : authNode.get("roles")) {
                    roles.add(roleNode.asText());
                }
            }

            List<GrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(authUsername, null, authorities);

            // Genera access token e refresh token
            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            ObjectNode response = mapper.createObjectNode();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("username", authUsername);
            response.set("roles", mapper.valueToTree(roles));

            return Response.ok(response.toString())
                    .header("Authorization", "Bearer " + accessToken)
                    .build();

        } catch (Exception e) {
            return Response.status(500)
                    .entity("{\"message\": \"Error logging in: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Path("/refresh")
    public Response refreshToken(JsonNode body) {
        try {
            String refreshToken = body.get("refreshToken").asText();

            if (!jwtTokenProvider.validateToken(refreshToken)) {
                return Response.status(401)
                        .entity("{\"message\": \"Invalid Refresh Token\"}")
                        .build();
            }

            String username = jwtTokenProvider.getUsername(refreshToken);
            String rolesStr = jwtTokenProvider.getRoles(refreshToken);
            List<GrantedAuthority> authorities = new ArrayList<>();
            if (rolesStr != null && !rolesStr.isEmpty()) {
                authorities = Arrays.stream(rolesStr.split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode response = mapper.createObjectNode();
            response.put("accessToken", newAccessToken);
            response.put("username", username);
            assert rolesStr != null;
            response.set("roles", mapper.valueToTree(Arrays.asList(rolesStr.split(","))));

            return Response.ok(response.toString())
                    .header("Authorization", "Bearer " + newAccessToken)
                    .build();

        } catch (Exception e) {
            return Response.status(500)
                    .entity("{\"message\": \"Error refreshing token: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/{username}")
    public Response getUser(@PathParam("username") String username) {
        String URL = authorizationServiceUrl + "/" + username;
        try {
            Client client = ClientBuilder.newClient();
            String responseBody = client.target(URL)
                    .request(MediaType.APPLICATION_JSON)
                    .get(String.class);

            return Response.ok(responseBody).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError()
                    .entity("{\"message\": \"Error calling APIs: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{username}")

    public Response deleteUser(@PathParam("username") String username) {
        String URL = authorizationServiceUrl + "/" + username;
        Client client = newClient();
        return client.target(URL)
                .request(MediaType.APPLICATION_JSON)
                .delete();
    }

    @POST
    @Path("/{username}/builds/{buildId}")
    public Response addBuildToUser(@PathParam("username") String username,
                                   @PathParam("buildId") Long buildId) {
        String URL = authorizationServiceUrl + "/" + username + "/builds/" + buildId;
        try {
            Client client = ClientBuilder.newClient();
            return client.target(URL)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json("{}")); // corpo vuoto, POST senza dati
        } catch (Exception e) {
            return Response.status(500)
                    .entity("{\"message\": \"Error adding build: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{username}/builds/{buildId}")
    public Response removeBuildFromUser(@PathParam("username") String username,
                                        @PathParam("buildId") Long buildId) {
        String URL = authorizationServiceUrl + "/" + username + "/builds/" + buildId;
        try {
            Client client = ClientBuilder.newClient();
            return client.target(URL)
                    .request(MediaType.APPLICATION_JSON)
                    .delete();
        } catch (Exception e) {
            return Response.status(500)
                    .entity("{\"message\": \"Error deleting build: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/{username}/builds")
    public Response getUserBuilds(@PathParam("username") String username) {
        String URL = authorizationServiceUrl + "/" + username + "/builds";
        try {
            Client client = ClientBuilder.newClient();
            String responseBody = client.target(URL)
                    .request(MediaType.APPLICATION_JSON)
                    .get(String.class);

            return Response.ok(responseBody).build();
        } catch (Exception e) {
            return Response.status(500)
                    .entity("{\"message\": \"Error fetching build: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @PUT
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUser(@PathParam("username") String username, JsonNode user) {
        try {
            String URL = authorizationServiceUrl + "/" + username;
            Client client = newClient();

            // 🔹 Inoltra la PUT originale al microservizio di autorizzazione
            Response backendResponse = client.target(URL)
                    .request(MediaType.APPLICATION_JSON)
                    .put(Entity.entity(user, MediaType.APPLICATION_JSON));

            // 🔹 Se il backend restituisce un errore, lo rimandiamo al client
            if (backendResponse.getStatus() >= 400) {
                return Response.status(backendResponse.getStatus())
                        .entity(backendResponse.readEntity(String.class))
                        .build();
            }

            // 🔹 Se l'utente ora ha il ruolo TECHNICIAN, rigenera il token
            if (user.has("roles") && user.get("roles").toString().contains("TECHNICIAN")) {
                List<String> roles = new ArrayList<>();
                user.get("roles").forEach(roleNode -> roles.add(roleNode.asText()));

                // Crea nuovi token con i nuovi ruoli
                String newAccessToken = jwtTokenProvider.createToken(username, roles);
                String newRefreshToken = jwtTokenProvider.createRefreshToken(username);

                ObjectNode tokenResponse = new ObjectMapper().createObjectNode();
                tokenResponse.put("accessToken", newAccessToken);
                tokenResponse.put("refreshToken", newRefreshToken);

                return Response.ok(tokenResponse).build();
            }

            // 🔸 Altrimenti, ritorna solo la risposta originale
            return Response.status(backendResponse.getStatus())
                    .entity(backendResponse.readEntity(String.class))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError()
                    .entity("Error updating user: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/all")
    public Response getAllUsers() {
        String URL = authorizationServiceUrl + "/all";
        Client client = newClient();
        Response response = client.target(URL)
                .request(MediaType.APPLICATION_JSON)
                .get();

        if (response.getStatus() == 404) {
            // Restituisci una lista vuota con status 200
            return Response.ok("[]", MediaType.APPLICATION_JSON).build();
        }
        return response;
    }

    @GET
    @Path("/users/role/{role}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersByRole(@PathParam("role") String role) {
        // Costruisci l'URL del microservizio utenti
        String url = authorizationServiceUrl + "/role/" + role;

        try (Client client = ClientBuilder.newClient()) {
            Response remote = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            int status = remote.getStatus();
            String responseBody = remote.readEntity(String.class);

            return Response.status(status)
                    .entity(responseBody)
                    .build();
        }
    }

    // --- CHAT GATEWAY CONTROLLER ---
    @POST
    @Path("/conversation")
    public Response createConversation(JsonNode payload) {
        // Trasforma il payload del frontend in quello accettato dal microservizio
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode newPayload = mapper.createObjectNode();
        if (payload.has("participants") && payload.get("participants").isArray()) {
            newPayload.put("user1Id", payload.get("participants").get(0).asText());
            newPayload.put("user2Id", payload.get("participants").get(1).asText());
        }

        Client client = ClientBuilder.newClient();
        Response remote = client.target(chatServiceUrl + "/conversation")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(newPayload.toString()));
        String body = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(body).build();
    }

    @GET
    @Path("/conversations/{userId}")
    public Response getUserConversations(@PathParam("userId") String userId) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(chatServiceUrl + "/conversations/" + userId)
                .request(MediaType.APPLICATION_JSON)
                .get();
        String body = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(body).build();
    }

    @GET
    @Path("/messages/{conversationId}")
    public Response getConversationMessages(@PathParam("conversationId") String conversationId) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(chatServiceUrl + "/messages/" + conversationId)
                .request(MediaType.APPLICATION_JSON)
                .get();
        String body = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(body).build();
    }

    @POST
    @Path("/message")
    public Response sendMessage(JsonNode payload) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(chatServiceUrl + "/message")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(payload.toString()));
        String body = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(body).build();
    }

    @PUT
    @Path("/conversation/{conversationId}/read/{userId}")
    public Response markAsRead(@PathParam("conversationId") String conversationId,
                               @PathParam("userId") String userId) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(chatServiceUrl + "/conversation/" + conversationId + "/read/" + userId)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json("{}"));
        String body = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(body).build();
    }

    @GET
    @Path("/conversation/{conversationId}/unread/{userId}")
    public Response countUnread(@PathParam("conversationId") String conversationId,
                                @PathParam("userId") String userId) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(chatServiceUrl + "/conversation/" + conversationId + "/unread/" + userId)
                .request(MediaType.APPLICATION_JSON)
                .get();
        String body = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(body).build();
    }

    @GET
    @Path("/conversation/{conversationId}/participants")
    public Response getConversationParticipantsGateway(@PathParam("conversationId") String conversationId) {
        // Crea client JAX-RS per chiamare il microservizio chat
        try (Client client = ClientBuilder.newClient()) {

            Response remote = client.target(chatServiceUrl + "/conversation/" + conversationId + "/partecipants")
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            // Legge la risposta dal microservizio
            String responseBody = remote.readEntity(String.class);

            // Restituisce lo stesso status code e corpo al client
            return Response.status(remote.getStatus()).entity(responseBody).build();
        }
    }

    @DELETE
    @Path("/conversation/{conversationId}")
    public Response deleteConversation(@PathParam("conversationId") String conversationId) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(chatServiceUrl + "/conversation/" + conversationId)
                .request(MediaType.APPLICATION_JSON)
                .delete();
        String body = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(body).build();
    }

    // --- GEN/COMP CONTROLLER ---

    @POST
    @Path("/build/create")
    public Response createBuild(JsonNode build) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(buildServiceUrl + "/create")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(build));

        String responseBody = remote.readEntity(String.class);
        client.close();

        return Response.status(remote.getStatus())
                .entity(responseBody)
                .build();
    }

    @POST
    @Path("/build/component")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    //
    public Response createComponent(JsonNode component) {
        Client client = ClientBuilder.newClient();
        System.out.println(buildServiceUrl + "/component");
        Response remote = client.target(buildServiceUrl + "/component")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(component));  // Invia il JsonNode direttamente

        String responseBody = remote.readEntity(String.class);
        client.close();

        return Response.status(remote.getStatus())
                .entity(responseBody)
                .build();
    }

    @POST
    @Path("/build/{id}/add")
    public Response addComponent(@PathParam("id") Long id, JsonNode part) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(buildServiceUrl + "/" + id + "/add")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(part));

        String responseBody = remote.readEntity(String.class);
        client.close();

        return Response.status(remote.getStatus())
                .entity(responseBody)
                .build();
    }

    @GET
    @Path("/build/{id}")
    public Response getBuild(@PathParam("id") Long id) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(buildServiceUrl + "/" + id)
                .request(MediaType.APPLICATION_JSON)
                .get();

        String responseBody = remote.readEntity(String.class);
        client.close();

        return Response.status(remote.getStatus())
                .entity(responseBody)
                .build();
    }

    @GET
    @Path("/build/compare")
    public Response compareBuilds(@QueryParam("id1") Long id1, @QueryParam("id2") Long id2) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(buildServiceUrl + "/compare")
                .queryParam("id1", id1)
                .queryParam("id2", id2)
                .request(MediaType.APPLICATION_JSON)
                .get();

        String responseBody = remote.readEntity(String.class);
        client.close();

        return Response.status(remote.getStatus())
                .entity(responseBody)
                .build();
    }

    @GET
    @Path("/build/component/compare")
    @Produces(MediaType.APPLICATION_JSON)
    public Response compareComponents(@QueryParam("id1") Long id1, @QueryParam("id2") Long id2) {
        Client client = ClientBuilder.newClient();

        Response remote = client.target(buildServiceUrl + "/component/compare")
                .queryParam("id1", id1)
                .queryParam("id2", id2)
                .request(MediaType.APPLICATION_JSON)
                .get();

        String responseBody = remote.readEntity(String.class);
        client.close();

        return Response.status(remote.getStatus())
                .entity(responseBody)
                .build();
    }

    @GET
    @Path("/build/generate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateBuildByBudget(@QueryParam("budget") float budget) {
        if (budget <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Budget must be greater than zero.\"}")
                    .build();
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("budget", budget);

        try (Client client = ClientBuilder.newClient()) {
            Response remote = client.target(buildServiceUrl + "/generate")
                    .queryParam("budget", budget) // BuildGenerator legge dal QueryParam
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(requestBody)); // POST al microservizio

            int status = remote.getStatus();
            String responseBody = remote.readEntity(String.class);

            return Response.status(status)
                    .entity(responseBody)
                    .build();
        }
    }

    @PUT
    @Path("/build/{id}")
    public Response updateBuild(@PathParam("id") Long id, JsonNode build) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(buildServiceUrl + "/" + id)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(build));

        String responseBody = remote.readEntity(String.class);
        client.close();

        return Response.status(remote.getStatus())
                .entity(responseBody)
                .build();
    }

    @DELETE
    @Path("/build/{id}")
    public Response deleteBuild(@PathParam("id") Long id) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(buildServiceUrl + "/" + id)
                .request(MediaType.APPLICATION_JSON)
                .delete();

        String responseBody = remote.readEntity(String.class);
        client.close();

        return Response.status(remote.getStatus())
                .entity(responseBody)
                .build();
    }

    @DELETE
    @Path("/build/component/{id}")
    public Response deleteComponent(@PathParam("id") Long id) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(buildServiceUrl + "/component/" + id)
                .request(MediaType.APPLICATION_JSON)
                .delete();

        String responseBody = remote.readEntity(String.class);
        client.close();

        return Response.status(remote.getStatus())
                .entity(responseBody)
                .build();
    }

    @GET
    @Path("/build/components")
    public Response getComponentsBySpecification(@QueryParam("column") String column, @QueryParam("value") String value) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(buildServiceUrl + "/components")
                .queryParam("column", column)
                .queryParam("value", value)
                .request(MediaType.APPLICATION_JSON)
                .get();

        String responseBody = remote.readEntity(String.class);
        client.close();

        return Response.status(remote.getStatus())
                .entity(responseBody)
                .build();
    }

    @GET
    @Path("/build/by-spec")
    public Response getBuildsBySpecification(@QueryParam("column") String column, @QueryParam("value") String value) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(buildServiceUrl + "/by-spec")
                .queryParam("column", column)
                .queryParam("value", value)
                .request(MediaType.APPLICATION_JSON)
                .get();

        String responseBody = remote.readEntity(String.class);
        client.close();

        return Response.status(remote.getStatus())
                .entity(responseBody)
                .build();
    }

    // --- COMMENT SERVICE ---

    @POST
    @Path("/comments")
    public Response createComment(JsonNode comment) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(commentServiceUrl)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(comment));

        String responseBody = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(responseBody).build();
    }

    @GET
    @Path("/comments/{targetType}/{targetId}")
    public Response getComments(@PathParam("targetType") String targetType,
                                @PathParam("targetId") String targetId) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(commentServiceUrl + "/" + targetType + "/" + targetId)
                .request(MediaType.APPLICATION_JSON)
                .get();

        String responseBody = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(responseBody).build();
    }

    @POST
    @Path("/comments/{id}/reply")
    public Response replyToComment(@PathParam("id") String parentId, JsonNode reply) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(commentServiceUrl + "/" + parentId + "/reply")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(reply));

        String responseBody = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(responseBody).build();
    }

    @DELETE
    @Path("/comments/{id}")
    public Response deleteComment(@PathParam("id") String id) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(commentServiceUrl + "/" + id)
                .request(MediaType.APPLICATION_JSON)
                .delete();

        String responseBody = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(responseBody).build();
    }

    // --- POST SERVICE ---

    @POST
    @Path("/posts")
    public Response createPost(JsonNode post) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(postServiceUrl)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(post));

        String responseBody = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(responseBody).build();
    }

    @GET
    @Path("/posts/{id}")
    public Response getPost(@PathParam("id") String id) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(postServiceUrl + "/" + id)
                .request(MediaType.APPLICATION_JSON)
                .get();

        String responseBody = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(responseBody).build();
    }

    @GET
    @Path("/posts/by/{userId}")
    public Response getPostsByUser(@PathParam("userId") String userId) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(postServiceUrl + "/by/" + userId)
                .request(MediaType.APPLICATION_JSON)
                .get();

        String responseBody = remote.readEntity(String.class);
        client.close();

        return Response.status(remote.getStatus()).entity(responseBody).build();
    }

    @GET
    @Path("/posts")
    public Response getAllPosts() {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(postServiceUrl)
                .request(MediaType.APPLICATION_JSON)
                .get();

        String responseBody = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(responseBody).build();
    }

    @DELETE
    @Path("/posts/{id}")
    public Response deletePost(@PathParam("id") String id) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(postServiceUrl + "/" + id)
                .request(MediaType.APPLICATION_JSON)
                .delete();

        String responseBody = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(responseBody).build();
    }

    // --- LIKE SERVICE ---

    /**
     * Aggiunge un like a un target (POST, COMMENT, TECHNICIAN)
     */
    @POST
    @Path("/likes")
    public Response createLike(JsonNode like) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(likeServiceUrl)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(like));

        String responseBody = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(responseBody).build();
    }

    /**
     * Rimuove un like tramite l'id
     */
    @DELETE
    @Path("/likes/{id}")
    public Response deleteLike(@PathParam("id") String id) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(likeServiceUrl + "/" + id)
                .request(MediaType.APPLICATION_JSON)
                .delete();

        String responseBody = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(responseBody).build();
    }

    /**
     * Lista tutti i like di un target
     */
    @GET
    @Path("/likes/{targetType}/{targetId}")
    public Response getLikes(@PathParam("targetType") String targetType,
                             @PathParam("targetId") String targetId) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(likeServiceUrl + "/" + targetType + "/" + targetId)
                .request(MediaType.APPLICATION_JSON)
                .get();

        String responseBody = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(responseBody).build();
    }

    // --- TECH CONTROLLER ---

    @GET
    @Path("/technicians")
    public Response getAllTechnicians() {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(saldoTecnicoServiceUrl)
                .request(MediaType.APPLICATION_JSON)
                .get();

        String responseBody = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(responseBody).build();
    }

    @GET
    @Path("/technicians/{userId}")
    @HasRole("TECHNICIAN")
    public Response getTechnicianByUserId(@PathParam("userId") String userId) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(saldoTecnicoServiceUrl + "/" + userId)
                .request(MediaType.APPLICATION_JSON)
                .get();

        String responseBody = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(responseBody).build();
    }

    @POST
    @Path("/technicians")
    @HasRole("TECHNICIAN")
    public Response createOrUpdateTechnician(JsonNode technicianProfile) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(saldoTecnicoServiceUrl)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(technicianProfile));

        String responseBody = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(responseBody).build();
    }

    @POST
    @Path("/technicians/{userId}/points/add")
    public Response addPoints(@PathParam("userId") String userId,
                              @QueryParam("amount") int amount,
                              @QueryParam("reason") String reason) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(saldoTecnicoServiceUrl + "/" + userId + "/points/add")
                .queryParam("amount", amount)
                .queryParam("reason", reason)
                .request(MediaType.APPLICATION_JSON)
                .post(null); // Nessun corpo richiesto

        String responseBody = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(responseBody).build();
    }

    @POST
    @Path("/technicians/{userId}/rating/add")
    public Response addRatingPointsGateway(@PathParam("userId") String userId,
                                           @QueryParam("amount") int amount,
                                           @QueryParam("reason") @DefaultValue("rating") String reason) {
        try (Client client = ClientBuilder.newClient()) {
            // Costruisce la chiamata al microservizio SaldoTecnico
            Response remote = client.target(saldoTecnicoServiceUrl + "/" + userId + "/rating/add")
                    .queryParam("amount", amount)
                    .queryParam("reason", reason)
                    .request(MediaType.APPLICATION_JSON)
                    .post(null); // Nessun corpo JSON richiesto

            // Legge la risposta dal microservizio
            String responseBody = remote.readEntity(String.class);

            // Restituisce lo stesso status code e corpo al client
            return Response.status(remote.getStatus()).entity(responseBody).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error assigning review.\"}")
                    .build();
        }
    }

    @GET
    @Path("/technicians/{userId}/rating")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRatingPointsGateway(@PathParam("userId") String userId) {
        try (Client client = ClientBuilder.newClient()) {
            // Chiamata al microservizio SaldoTecnico
            Response remote = client.target(saldoTecnicoServiceUrl + "/" + userId + "/rating")
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            // Legge la risposta del microservizio
            String responseBody = remote.readEntity(String.class);

            // Restituisce lo stesso status code e corpo
            return Response.status(remote.getStatus()).entity(responseBody).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error fetching rating.\"}")
                    .build();
        }
    }

    @POST
    @Path("/technicians/{userId}/points/spend")
    public Response spendPoints(@PathParam("userId") String userId,
                                @QueryParam("amount") int amount,
                                @QueryParam("reason") String reason) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(saldoTecnicoServiceUrl + "/" + userId + "/points/spend")
                .queryParam("amount", amount)
                .queryParam("reason", reason)
                .request(MediaType.APPLICATION_JSON)
                .post(null); // Nessun corpo richiesto

        String responseBody = remote.readEntity(String.class);
        client.close();
        return Response.status(remote.getStatus()).entity(responseBody).build();
    }

    @DELETE
    @Path("/technicians/{userId}/deleteTechnician")
    public Response deleteTechnician(@PathParam("userId") String userId) {
        Client client = ClientBuilder.newClient();
        Response remote = client.target(saldoTecnicoServiceUrl + "/" + userId + "/deleteTechnician")
                .request(MediaType.APPLICATION_JSON)
                .delete();

        String responseBody = remote.readEntity(String.class);
        client.close();

        return Response.status(remote.getStatus()).entity(responseBody).build();
    }

}