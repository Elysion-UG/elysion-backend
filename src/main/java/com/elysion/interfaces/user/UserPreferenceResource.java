package com.elysion.interfaces.user;

import com.elysion.application.user.UserSustainabilityPrefService;
import com.elysion.application.user.UserService;
import com.elysion.domain.user.Importance;
import com.elysion.domain.user.User;
import com.elysion.domain.user.UserSustainabilityPref;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
@Path("/users/preferences")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("User")
@Tag(name = "User Preferences", description = "Nachhaltigkeits-Präferenzen je Nutzer")
@SecurityRequirement(name = "bearerAuth")
public class UserPreferenceResource {

    @Inject
    UserService userService;

    @Inject
    UserSustainabilityPrefService prefService;

    // --- DTOs ---
    @Schema(name = "SetPreferenceRequest", description = "Wert für eine Präferenz setzen")
    public static class SetPreferenceRequest {
        @NotNull
        public Importance importance;
    }

    @Schema(name = "PreferenceDTO", description = "Repräsentation einer Nutzerpräferenz")
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
    @Operation(summary = "Alle Präferenzen abrufen",
            description = "Gibt eine Liste aller gesetzten Präferenzen des eingeloggten Nutzers zurück.")
    @APIResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PreferenceDTO.class),
                    examples = @ExampleObject(
                            value = "[{\"filterKey\":\"bio\",\"importance\":\"IMPORTANT\"}," +
                                    "{\"filterKey\":\"ethical-work\",\"importance\":\"NICE_TO_HAVE\"}]"
                    )
            )
    )
    public Response getAll(@Context SecurityContext ctx) {
        User user = currentUserOr404(ctx);
        List<UserSustainabilityPref> prefs = prefService.getPreferences(user);
        List<PreferenceDTO> dtos = prefs.stream().map(PreferenceDTO::from).toList();
        return Response.ok(dtos).build();
    }

    // GET /users/preferences/map -> Map filterKey -> Importance (praktisch fürs FE)
    @GET
    @Path("/map")
    @Operation(summary = "Präferenzen als Map abrufen",
            description = "Liefert eine Map von Filter-Key zu Importance.")
    @APIResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class),
                    examples = @ExampleObject(value = "{\"bio\":\"IMPORTANT\",\"ethical-work\":\"NICE_TO_HAVE\"}")
            )
    )
    public Response getMap(@Context SecurityContext ctx) {
        User user = currentUserOr404(ctx);
        Map<String, Importance> map = prefService.getPreferenceMap(user);
        return Response.ok(map).build();
    }

    // GET /users/preferences/{filterKey} -> einzelne Präferenz
    @GET
    @Path("/{filterKey}")
    @Operation(summary = "Einzelne Präferenz abrufen",
            description = "Liefert die Präferenz für den angegebenen Filter-Key.")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PreferenceDTO.class),
                            examples = @ExampleObject(value = "{\"filterKey\":\"bio\",\"importance\":\"IMPORTANT\"}")
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Nicht gefunden",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(value = "{\"error\":\"No preference for filter: bio\"}")
                    )
            )
    })
    public Response getOne(
            @Parameter(description = "Filter-Schlüssel", required = true, examples = @ExampleObject(value = "bio"))
            @PathParam("filterKey") String filterKey,
            @Context SecurityContext ctx
    ) {
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
    @Operation(summary = "Präferenz setzen/ändern",
            description = "Setzt die Importance für einen Filter-Key. Erzeugt oder überschreibt die Präferenz.")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PreferenceDTO.class),
                            examples = @ExampleObject(value = "{\"filterKey\":\"bio\",\"importance\":\"IMPORTANT\"}")
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Ungültiger Filter-Key oder Payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(value = "{\"error\":\"Unknown filter key: foo\"}")
                    )
            )
    })
    public Response setPreference(
            @Parameter(description = "Filter-Schlüssel", required = true, examples = @ExampleObject(value = "bio"))
            @PathParam("filterKey") String filterKey,
            @Valid @org.eclipse.microprofile.openapi.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SetPreferenceRequest.class),
                            examples = @ExampleObject(value = "{\"importance\":\"IMPORTANT\"}")
                    )
            )
            SetPreferenceRequest req,
            @Context SecurityContext ctx
    ) {
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
    public Response deletePreference(
            @Parameter(description = "Filter-Schlüssel", required = true, examples = @ExampleObject(value = "bio"))
            @PathParam("filterKey") String filterKey,
            @Context SecurityContext ctx
    ) {
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