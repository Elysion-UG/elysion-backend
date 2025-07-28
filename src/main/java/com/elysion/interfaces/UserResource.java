package com.elysion.interfaces;

import com.elysion.application.UserService;
import com.elysion.domain.User;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

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
        LOG.info("login request: " + request.toString());
        try {
            User user = userService.authenticate(request.email, request.password);
            LOG.info("login successful");
            return Response.ok().entity("Login successful for " + user.email).build();
        } catch (IllegalArgumentException e) {
            LOG.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }
}