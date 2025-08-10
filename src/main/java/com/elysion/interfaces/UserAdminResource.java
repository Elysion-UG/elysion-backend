package com.elysion.interfaces;

import com.elysion.application.UserService;
import com.elysion.domain.User;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.UUID;

@Path("/users")
public class UserAdminResource {

    @Inject
    UserService userService;

    @PUT
    @Path("/{id}/role/seller")
    @RolesAllowed("Admin")
    @Transactional
    public Response makeSeller(@PathParam("id") UUID userId) {
        try {
            User u = userService.promoteToSeller(userId);
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
}