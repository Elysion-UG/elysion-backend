package com.elysion.interfaces;

import com.elysion.application.UserSustainabilityPrefService;
import com.elysion.application.UserService;
import com.elysion.domain.Importance;
import com.elysion.domain.User;
import com.elysion.domain.UserSustainabilityPref;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.Map;
@Path("/users/preferences")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("User")
public class UserPreferenceResource {

    @Inject
    UserService userService;

    @Inject
    UserSustainabilityPrefService prefService;

    // --- DTOs ---
    public static class SetPreferenceRequest {
        @NotNull
        public Importance importance;
    }

    public static class PreferenceDTO {
        public String filterKey;
        public Importance importance;

        public static PreferenceDTO from(UserSustainabilityPref p) {
            PreferenceDTO dto = new PreferenceDTO();
            dto.filterKey = p.filter.key;
            dto.importance = p.importance;
            return dto;
        }
    }

    // Hilfsfunktion: aktuellen User laden
    private User currentUserOr404(SecurityContext ctx) {
        String email = ctx.getUserPrincipal() != null ? ctx.getUserPrincipal().getName() : null;
        if (email == null) {
            throw new WebApplicationException("No principal", Response.Status.UNAUTHORIZED);
        }
        User u = userService.findByEmail(email);
        if (u == null) {
            throw new WebApplicationException("User not found", Response.Status.NOT_FOUND);
        }
        return u;
    }

    // GET /users/preferences  -> alle Präferenzen des Users (als Liste)
    @GET
    public Response getAll(@Context SecurityContext ctx) {
        User user = currentUserOr404(ctx);
        List<UserSustainabilityPref> prefs = prefService.getPreferences(user);
        List<PreferenceDTO> dtos = prefs.stream().map(PreferenceDTO::from).toList();
        return Response.ok(dtos).build();
    }

    // GET /users/preferences/map -> Map filterKey -> Importance (praktisch fürs FE)
    @GET
    @Path("/map")
    public Response getMap(@Context SecurityContext ctx) {
        User user = currentUserOr404(ctx);
        Map<String, Importance> map = prefService.getPreferenceMap(user);
        return Response.ok(map).build();
    }

    // GET /users/preferences/{filterKey} -> einzelne Präferenz
    @GET
    @Path("/{filterKey}")
    public Response getOne(@PathParam("filterKey") String filterKey,
                           @Context SecurityContext ctx) {
        User user = currentUserOr404(ctx);
        return prefService.getPreference(user, filterKey)
                .map(p -> Response.ok(PreferenceDTO.from(p)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "No preference for filter: " + filterKey))
                        .build());
    }

    // PUT /users/preferences/{filterKey}  Body: { "importance": "IMPORTANT" }
    @PUT
    @Path("/{filterKey}")
    public Response setPreference(@PathParam("filterKey") String filterKey,
                                  @Valid SetPreferenceRequest req,
                                  @Context SecurityContext ctx) {
        User user = currentUserOr404(ctx);
        try {
            UserSustainabilityPref saved = prefService.setPreference(user, filterKey, req.importance);
            return Response.ok(PreferenceDTO.from(saved)).build();
        } catch (IllegalArgumentException iae) {
            // z.B. unbekannter filterKey
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", iae.getMessage()))
                    .build();
        }
    }

    // DELETE /users/preferences/{filterKey}
    @DELETE
    @Path("/{filterKey}")
    public Response deletePreference(@PathParam("filterKey") String filterKey,
                                     @Context SecurityContext ctx) {
        User user = currentUserOr404(ctx);
        boolean deleted = prefService.removePreference(user, filterKey);
        if (deleted) {
            return Response.noContent().build(); // 204
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "No preference to delete for filter: " + filterKey))
                .build();
    }
}