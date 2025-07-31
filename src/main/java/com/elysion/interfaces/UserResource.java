package com.elysion.interfaces;

import com.elysion.application.UserService;
import com.elysion.domain.User;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jboss.logging.Logger;

import java.time.OffsetDateTime;
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
        @NotBlank @Email
        public String email;
        @NotBlank @Size(min=8)
        public String password;
    }

    public static class LoginRequest {
        @NotBlank @Email
        public String email;
        @NotBlank @Size(min=8)
        public String password;
    }

    @POST
    @Path("/register")
    @Transactional
    public Response register(@Valid RegisterRequest request) {
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
    @PermitAll
    public Response login(@Valid LoginRequest request) {
        try {
            User user = userService.authenticate(request.email, request.password);
            String token = userService.generateJwt(user);
            return Response.ok(Map.of("token", token))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/confirm")
    @PermitAll
    public Response confirm(@QueryParam("token") String token) {
        if (token == null || token.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Token is missing")).build();
        }

        User user = User.find("activationToken", token).firstResult();
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Invalid token")).build();
        }

        // Optional: Ablaufprüfung
        if (user.activationTokenCreated != null &&
                user.activationTokenCreated.isBefore(OffsetDateTime.now().minusHours(24))) {
            return Response.status(Response.Status.GONE)
                    .entity(Map.of("error", "Token expired")).build();
        }

        user.active = true;
        user.activationToken = null;
        user.activationTokenCreated = null;
        user.persist();

        return Response.ok(Map.of("message", "Account successfully activated")).build();
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