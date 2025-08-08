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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
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

import java.time.OffsetDateTime;
import java.util.Map;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@Path("/users")
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
public class UserResource {

    private static final Logger LOG = Logger.getLogger(UserService.class); // ← hier definierst du LOG

    @Inject
    UserService userService;

    // ======== DTOs ========
    @Schema(name = "RegisterRequest", description = "Payload zum Registrieren eines Users")
    public static class RegisterRequest {

        @Schema(required = true)
        @NotBlank @Email
        public String email;

        @Schema(required = true, minLength = 8)
        @NotBlank @Size(min=8)
        public String password;

        @Schema(required = true)
        @NotBlank
        public String firstName;

        @Schema(required = true)
        @NotBlank
        public String lastName;

        @Override public String toString() { return email + " " + firstName + " " + lastName; }
    }

    @Schema(name = "LoginRequest", description = "Credentials für Login")
    public static class LoginRequest {

        @Schema(required = true)
        @NotBlank @Email
        public String email;

        @Schema(required = true, minLength = 8)
        @NotBlank @Size(min=8)
        public String password;
    }

    @Schema(name = "ChangeProfileRequest", description = "Profiländerung")
    public static class ChangeProfileRequest {

        @Schema(required = true)
        @NotBlank
        public String firstName;

        @Schema(required = true)
        @NotBlank
        public String lastName;
    }

    @Schema(name = "ChangeEmailRequest", description = "E-Mail ändern (Bestätigung folgt per Double-Opt-In)")
    public static class ChangeEmailRequest {

        @Schema(required = true)
        @NotBlank @Email
        public String newEmail;
    }

    @Schema(name = "ChangePasswordRequest", description = "Passwort ändern")
    public static class ChangePasswordRequest {

        @Schema(required = true)
        @NotBlank
        public String currentPassword;

        @Schema(required = true, minLength = 8)
        @NotBlank @Size(min = 8)
        public String newPassword;
    }

    // ======== Endpoints ========



    @POST
    @Path("/register")
    @Transactional
    @Operation(
            summary = "Register new user",
            description = "Erzeugt einen neuen User und sendet ggf. eine Verifizierungs-E-Mail."
    )
    @RequestBody(
            description = "Registrierungsdaten",
            content = @Content(
                    schema = @Schema(implementation = RegisterRequest.class),
                    examples = {
                            @ExampleObject(name = "default",
                                    value = "{\"email\":\"alice@example.com\",\"password\":\"Str0ngP@ssword!\",\"firstName\":\"Alice\",\"lastName\":\"Doe\"}")
                    }
            )
    )
    @APIResponses({
            @APIResponse(responseCode = "201", description = "User erstellt",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = SchemaType.STRING),
                            examples = @ExampleObject(value = "\"<user-id>\""))),
            @APIResponse(responseCode = "409", description = "E-Mail bereits vergeben",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = SchemaType.STRING),
                            examples = @ExampleObject(value = "\"E-Mail already in use\"")))
    })
    public Response register(@Valid RegisterRequest request) {
        LOG.info("Register request: " + request.toString());
        try {
            User user = userService.register(request.email, request.password, request.firstName, request.lastName);
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
    @Operation(
            summary = "Login",
            description = "Authentifiziert den User und liefert ein JWT zurück."
    )
    @RequestBody(
            description = "Login-Daten",
            content = @Content(
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = @ExampleObject(value = "{\"email\":\"alice@example.com\",\"password\":\"Str0ngP@ssword!\"}")
            )
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Login erfolgreich",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            value = "{\"token\":\"<JWT>\"}"
                    )),
                    headers = {
                            @Header(name = HttpHeaders.AUTHORIZATION, description = "Bearer <JWT>", required = true)
                    }
            ),
            @APIResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\":\"Invalid credentials\"}"))
            )
    })
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
    @Path("/confirm-email")
    @PermitAll
    @Operation(
            summary = "E-Mail-Bestätigung durchführen",
            description = "Bestätigt eine ausstehende E-Mail-Änderung über den Token."
    )
    @APIResponses({
            @APIResponse(responseCode = "200", description = "E-Mail geändert",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\":\"E-Mail erfolgreich geändert\"}"))),
            @APIResponse(responseCode = "400", description = "Token fehlt",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\":\"Token is missing\"}"))),
            @APIResponse(responseCode = "404", description = "Ungültiger Token",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\":\"Invalid token\"}"))),
            @APIResponse(responseCode = "410", description = "Token abgelaufen",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\":\"Token expired\"}")))
    })
    public Response confirmEmail(@QueryParam("token") String token) {
        if (token == null || token.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error","Token is missing")).build();
        }
        User user = User.find("emailActivationToken", token).firstResult();
        if (user == null) {
            return Response.status(NOT_FOUND)
                    .entity(Map.of("error","Invalid token")).build();
        }
        // Optional: Ablauf prüfen wie in /confirm
        if (user.activationTokenCreated.isBefore(OffsetDateTime.now().minusHours(24))) {
            return Response.status(Response.Status.GONE)
                    .entity(Map.of("error","Token expired")).build();
        }

        // Tatsächlichen Wechsel jetzt durchführen
        user.email = user.pendingEmail;
        user.pendingEmail = null;
        user.activationToken = null;
        user.activationTokenCreated = null;
        user.persist();

        return Response.ok(Map.of("message","E-Mail erfolgreich geändert")).build();
    }

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
                    schema = @Schema(implementation = ChangeEmailRequest.class),
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
    public Response changeEmail(@Valid ChangeEmailRequest request, @Context SecurityContext ctx) {
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

    @PUT
    @Path("/password")
    @RolesAllowed("User")
    @Transactional
    @Operation(
            summary = "Passwort ändern",
            description = "Ändert das Passwort des eingeloggten Users."
    )
    @SecurityRequirement(name = "bearerAuth")
    @RequestBody(
            content = @Content(
                    schema = @Schema(implementation = ChangePasswordRequest.class),
                    examples = @ExampleObject(
                            value = "{\"currentPassword\":\"Str0ngP@ssword!\",\"newPassword\":\"Ev3nStr0nger!\"}"
                    )
            )
    )
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Passwort geändert",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\":\"Password updated\"}"))),
            @APIResponse(responseCode = "401", description = "Current password falsch",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\":\"Invalid current password\"}"))),
            @APIResponse(responseCode = "404", description = "User nicht gefunden")
    })
    public Response changePassword(@Valid ChangePasswordRequest request, @Context SecurityContext ctx) {
        String email = ctx.getUserPrincipal().getName();
        User user = userService.findByEmail(email);
        if (user == null) {
            return Response.status(NOT_FOUND).build();
        }
        try {
            userService.changePassword(user, request.currentPassword, request.newPassword);
            return Response.ok(Map.of("message", "Password updated")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/profile")
    @RolesAllowed("User")
    @Operation(
            summary = "Profil ändern",
            description = "Ändert Vor- und Nachnamen des eingeloggten Users."
    )
    @SecurityRequirement(name = "bearerAuth")
    @RequestBody(
            content = @Content(
                    schema = @Schema(implementation = ChangeProfileRequest.class),
                    examples = @ExampleObject(value = "{\"firstName\":\"Alice\",\"lastName\":\"Doe\"}")
            )
    )
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Profil aktualisiert",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\":\"Profile updated\"}"))),
            @APIResponse(responseCode = "404", description = "User nicht gefunden")
    })
    public Response changeProfile(@Valid ChangeProfileRequest req,
                                  @Context SecurityContext ctx) {
        User user = userService.findByEmail(ctx.getUserPrincipal().getName());
        if (user == null) {
            return Response.status(NOT_FOUND).build();
        }
        user.firstName = req.firstName;
        user.lastName = req.lastName;
        user.persist();
        return Response.ok(Map.of("message","Profile updated")).build();
    }

    @GET
    @Path("/me")
    @RolesAllowed("User")
    @Operation(
            summary = "Eigene User-Details",
            description = "Liefert das User-Objekt des eingeloggten Users."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @APIResponse(responseCode = "404", description = "User nicht gefunden")
    })
    public Response me(@Context SecurityContext ctx) {
        String email = ctx.getUserPrincipal().getName();
        User user = userService.findByEmail(email);  // gibt null zurück, wenn nicht gefunden
        if (user == null) {
            // Kein User mit dieser E‑Mail – 404 Not Found
            return Response.status(NOT_FOUND).build();
        }
        return Response.ok(user).build();
    }
}