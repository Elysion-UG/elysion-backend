package com.elysion.interfaces.user;

import com.elysion.application.user.UserService;
import com.elysion.domain.user.User;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.Map;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@Path("/userprofile")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "User management API")
@SecuritySchemes({
        @SecurityScheme(
                securitySchemeName = "bearerAuth",
                type = SecuritySchemeType.HTTP,
                scheme = "bearer",
                bearerFormat = "JWT"
        )
})
public class UserEditProfileResource {

    private static final Logger LOG = Logger.getLogger(UserEditProfileResource.class);

    @Inject
    UserService userService;

    @PUT
    @Path("/email")
    @RolesAllowed("User")
    @Transactional
    @Operation(
            summary = "E-Mail ändern (Anstoß)",
            description = "Startet die E-Mail-Änderung. Die tatsächliche Umstellung erfolgt nach Bestätigung per Token."
    )
    @SecurityRequirement(name = "bearerAuth")
    @RequestBody(
            content = @Content(
                    schema = @Schema(implementation = UserResource.ChangeEmailRequest.class),
                    examples = @ExampleObject(value = "{\"newEmail\":\"newalice@example.com\"}")
            )
    )
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Änderung angestoßen",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\":\"E-Mail updated\"}"))),
            @APIResponse(responseCode = "404", description = "User nicht gefunden"),
            @APIResponse(responseCode = "409", description = "Konflikt (z. B. E-Mail belegt)",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\":\"E-Mail already in use\"}")))
    })
    public Response changeEmail(@Valid UserResource.ChangeEmailRequest request, @Context SecurityContext ctx) {
        String email = ctx.getUserPrincipal().getName();
        User user = userService.findByEmail(email);
        if (user == null) {
            return Response.status(NOT_FOUND).build();
        }
        try {
            userService.changeEmail(user, request.newEmail);
            return Response.ok(Map.of("message", "E-Mail updated")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT).entity(Map.of("error", e.getMessage())).build();
        }
    }


}
