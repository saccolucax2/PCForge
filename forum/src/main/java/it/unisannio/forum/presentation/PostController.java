package it.unisannio.forum.presentation;

import it.unisannio.forum.model.Post;
import it.unisannio.forum.service.PostService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Path("/posts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @POST
    public Response createPost(Post post) {
        Post saved = postService.create(post);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }

    @GET
    @Path("/{id}")
    public Response getPost(@PathParam("id") String id) {
        return postService.findById(id)
                .map(post -> Response.ok(post).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/by/{userId}")
    public List<Post> getPostsByUser(@PathParam("userId") String userId) {
        return postService.findByUserId(userId);
    }

    @GET
    public List<Post> getAllPosts() {
        return postService.findAll();
    }

    @DELETE
    @Path("/{id}")
    public Response deletePost(@PathParam("id") String id) {
        postService.deleteById(id);
        return Response.noContent().build();
    }
}