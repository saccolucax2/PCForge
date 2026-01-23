package it.unisannio.forum.presentation;

import it.unisannio.forum.model.Like;
import it.unisannio.forum.service.LikeService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Path("/likes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    /**
     * Aggiunge un like a un target (POST, COMMENT, TECHNICIAN)
     */
    @POST
    public Response createLike(Like like) {
        Like saved = likeService.create(like);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }

    /**
     * Rimuove un like tramite l'id
     */
    @DELETE
    @Path("/{id}")
    public Response deleteLike(@PathParam("id") String id) {
        likeService.deleteById(id);
        return Response.noContent().build();
    }

    /**
     * Lista tutti i like di un target
     */
    @GET
    @Path("/{targetType}/{targetId}")
    public List<Like> getLikes(@PathParam("targetType") String targetType,
                               @PathParam("targetId") String targetId) {
        return likeService.findByTarget(targetType, targetId);
    }
}