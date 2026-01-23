package it.unisannio.saldotecnico.presentation;

import it.unisannio.saldotecnico.model.TechnicianProfile;
import it.unisannio.saldotecnico.application.TechnicianService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
@Path("/technicians")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TechnicianController {

    private final TechnicianService service = new TechnicianService();

    /**
     * Recupera tutti i profili dei tecnici.
     */
    @GET
    public List<TechnicianProfile> getAllTechnicians() {
        return service.getAllProfiles();
    }

    /**
     * Recupera un tecnico tramite userId.
     */
    @GET
    @Path("/{userId}")
    public Response getTechnicianByUserId(@PathParam("userId") String userId) {
        try {
            TechnicianProfile profile = service.getProfileByUserId(userId);
            return Response.ok(profile).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Technician with userId " + userId + " not found")
                    .build();
        }
    }

    /**
     * Crea o aggiorna un profilo tecnico.
     */
    @POST
    public Response createOrUpdateTechnician(TechnicianProfile profile) {
        if (profile.getPointsBalance() == 0) profile.setPointsBalance(0);
        if (profile.getTransactions() == null) profile.setTransactions(new ArrayList<>());

        TechnicianProfile saved = service.createOrUpdateProfile(profile);
        return Response.ok(saved).build();
    }

    @POST
    @Path("/{userId}/rating/add")
    public Response addRatingPoints(@PathParam("userId") String userId,
                                    @QueryParam("amount") int amount,
                                    @QueryParam("reason") @DefaultValue("rating") String reason) {
        try {
            // Chiama il servizio che calcola la media e aggiorna il profilo
            service.addRatingPoints(userId, amount, reason);

            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error during rating points addition.\"}")
                    .build();
        }
    }

    @GET
    @Path("/{userId}/rating")
    public Response getRatingPoints(@PathParam("userId") String userId) {
        try {
            int ratingPoints = service.getRatingPoints(userId);
            return Response.ok("{\"ratingPoints\": " + ratingPoints + "}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Technician with userId " + userId + " not found")
                    .build();
        }
    }


    /**
     * Aggiunge punti al tecnico.
     */
    @POST
    @Path("/{userId}/points/add")
    public Response addPoints(@PathParam("userId") String userId,
                              @QueryParam("amount") int amount,
                              @QueryParam("reason") String reason) {
        try {
            TechnicianProfile updated = service.addPoints(userId, amount, reason);
            return Response.ok(updated).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    /**
     * Deduce punti dal tecnico.
     */
    @POST
    @Path("/{userId}/points/spend")
    public Response spendPoints(@PathParam("userId") String userId,
                                @QueryParam("amount") int amount,
                                @QueryParam("reason") String reason) {
        try {
            TechnicianProfile updated = service.spendPoints(userId, amount, reason);
            return Response.ok(updated).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Technician with userId " + userId + " not found")
                    .build();
        }
    }

    @DELETE
    @Path("/{userId}/deleteTechnician")
    public Response deleteTechnician(@PathParam("userId") String userId) {
        try {
            service.deleteTechnicianProfile(userId);
            return Response.ok("Technician with userId " + userId + " deleted successfully").build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Technician with userId " + userId + " not found")
                    .build();
        }
    }

}