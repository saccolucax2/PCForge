package it.unisannio.forum.presentation;

import it.unisannio.forum.model.Comment;
import it.unisannio.forum.service.CommentService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Path("/comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @POST
    public Response createComment(Comment comment) {

        if ("TECHNICIAN".equals(comment.getTargetType()) && comment.getRating() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Rating is required when commenting a technician")
                    .build();
        }

        Comment saved = commentService.create(comment);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }

    @GET
    @Path("/{targetType}/{targetId}")
    public List<Comment> getComments(@PathParam("targetType") String targetType,
                                     @PathParam("targetId") String targetId) {
        return commentService.findByTarget(targetType, targetId);
    }

    @POST
    @Path("/{id}/reply")
    public Response reply(@PathParam("id") String parentId, Comment reply) {
        Comment saved = commentService.replyToComment(parentId, reply);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteComment(@PathParam("id") String id) {
        commentService.deleteById(id);
        return Response.noContent().build();
    }
}