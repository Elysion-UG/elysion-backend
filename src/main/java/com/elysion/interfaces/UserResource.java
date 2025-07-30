package com.elysion.interfaces;

import com.elysion.application.UserService;
import com.elysion.domain.User;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.UUID;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private static final Logger LOG = Logger.getLogger(UserService.class); // ← hier definierst du LOG

    @Inject
    UserService userService;

    public static class RegisterRequest {
        public String email;
        public String password;
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }

    @POST
    @Path("/register")
    @Transactional
    public Response register(RegisterRequest request) {
        LOG.info("Register request: " + request.toString());
        try {
            User user = userService.register(request.email, request.password);
            LOG.info("Register successful");
            return Response.status(Response.Status.CREATED).entity(user.id).build();
        } catch (IllegalArgumentException e) {
            LOG.error(e.getMessage());
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        try {
            User user = userService.authenticate(request.email, request.password);
            String token = userService.generateJwt(user);
            return Response.ok()
                    .entity(Map.of("token", token))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/me")
    @RolesAllowed("User")
    public Response me(@Context SecurityContext ctx) {
        String email = ctx.getUserPrincipal().getName();
        User user = userService.findByEmail(email);  // gibt null zurück, wenn nicht gefunden
        if (user == null) {
            // Kein User mit dieser E‑Mail – 404 Not Found
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(user).build();
    }
}