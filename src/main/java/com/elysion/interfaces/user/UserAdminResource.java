package com.elysion.interfaces.user;

import com.elysion.application.user.UserService;
import com.elysion.domain.user.User;
import com.elysion.security.PasswordService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.UUID;

@Path("/users")
public class UserAdminResource {

    private static final Logger LOG = Logger.getLogger(UserAdminResource.class);

    @Inject
    UserService userService;

    @Inject
    PasswordService passwordService;


    public static class ReauthRequest {
        @NotBlank
        public String adminPassword; // Step-up Reauth
    }

    @PUT
    @Path("/{id}/role/seller")
    @RolesAllowed("Admin")
    @Transactional
    public Response makeSeller(@PathParam("id") UUID userId) {
        LOG.info("makeSeller called: " +  userId);
        try {
            User u = userService.promoteToSeller(userId);
            LOG.info("makeSeller called: " +  u);
            return Response.ok(Map.of(
                    "message", "Role updated to Seller",
                    "userId", u.id.toString()
            )).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{id}/role/admin")
    @Transactional
    public Response makeAdmin(@PathParam("id") UUID userId,
                              ReauthRequest body,
                              @Context SecurityContext ctx) {
        // Step-up: Admin muss eigenes Passwort bestätigen
        String actingEmail = ctx.getUserPrincipal().getName();
        User acting = userService.findByEmail(actingEmail);
        if (acting == null) return Response.status(Response.Status.UNAUTHORIZED).build();

        if (!passwordService.verifyPassword(body.adminPassword, acting.salt, acting.passwordHash)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error","Reauthentication failed")).build();
        }

        try {
            User updated = userService.promoteToAdmin(userId);
            // TODO: Audit-Log (acting.id, target.id, timestamp)
            return Response.ok(Map.of(
                    "message","User promoted to Admin",
                    "userId", updated.id.toString()
            )).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", e.getMessage())).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT).entity(Map.of("error", e.getMessage())).build();
        }
    }
}