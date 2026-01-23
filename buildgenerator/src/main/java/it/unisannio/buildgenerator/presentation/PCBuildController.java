package it.unisannio.buildgenerator.presentation;

import it.unisannio.buildgenerator.application.PCBuildService;
import it.unisannio.buildgenerator.model.PCBuild;
import it.unisannio.buildgenerator.model.Part;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.stereotype.Component;

@Component
@Path("/build")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PCBuildController {

    private final PCBuildService service;

    public PCBuildController(PCBuildService service) {
        this.service = service;
    }

    @POST
    @Path("/create")
    public Response createBuild(PCBuild build) {
        try {
            return Response.ok(service.createBuild(build)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/component")
    public Response createComponent(Part part) {
        try {
            return Response.ok(service.createComponent(part)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/{id}/add")
    public Response addComponent(@PathParam("id") Long id, Part part) {
        try {
            return Response.ok(service.addComponent(id, part)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getBuild(@PathParam("id") Long id) {
        try {
            return Response.ok(service.getBuild(id)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/generate")
    public Response generateBuild(@QueryParam("budget") float budget) {
        try {
            PCBuild build = service.generateOptimizedBuildByBudget(budget);
            return Response.ok(build).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/compare")
    public Response compareBuilds(@QueryParam("id1") Long id1Obj, @QueryParam("id2") Long id2Obj) {
        if (id1Obj == null || id2Obj == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Both IDs must be specified")
                    .build();
        }

        try {
            String result = service.compareBuilds(id1Obj, id2Obj);
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/component/compare")
    public Response compareComponents(
            @QueryParam("id1") Long id1,
            @QueryParam("id2") Long id2) {
        try {
            String result = service.compareComponents(id1, id2);
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error comparing: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateBuild(@PathParam("id") Long id, PCBuild build) {
        try {
            return Response.ok(service.updateBuild(id, build)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/component/{id}")
    public Response updateComponent(@PathParam("id") Long id, Part part) {
        try {
            // Assicurati che l'ID dell'oggetto sia impostato correttamente
            part.setId(id);

            // Aggiorna la componente
            Part updated = service.updateComponent(id, part);

            return Response.ok(updated).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteBuild(@PathParam("id") Long id) {
        try {
            service.deleteBuild(id);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/component/{id}")
    public Response deleteComponent(@PathParam("id") Long id) {
        try {
            service.deleteComponent(id);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/components")
    public Response getComponentsBySpecification(@QueryParam("column") String column, @QueryParam("value") String value) {
        return Response.ok(service.getComponentsBySpecification(column, value)).build();
    }

    @GET
    @Path("/by-spec")
    public Response getBuildsBySpecification(@QueryParam("column") String column, @QueryParam("value") String value) {
        return Response.ok(service.getBuildsBySpecification(column, value)).build();
    }
}