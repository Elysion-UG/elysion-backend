package com.elysion.interfaces.user;

import com.elysion.application.user.UserService;
import com.elysion.domain.user.User;
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

    private static final Logger LOG = Logger.getLogger(UserResource.class);

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

        @Override public String toString() { return email + " " + password; }
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
            summary = "Registriert einen neuen Nutzer",
            description = """
            Erzeugt einen neuen User und sendet eine Verifizierungs-Mail an die angegebene Adresse.
            Hier müssen wir aktuell noch einen Weg hinzufügen, wie wir dafür sorgen können, das die Präferenzen direkt mit geliefert und gespeichert werden können.
            Aktuell geht das nur nachdem der Nutzer sich Eingeloggt hat, aber er kann sich erst einloggen, nach dem Mail bestätigen. 
            Da muss es noch einen besseren Weg geben.
            """
    )
    @RequestBody(
            description = "Registrierungsdaten",
            content = @Content(
                    schema = @Schema(implementation = RegisterRequest.class),
                    examples = {
                            @ExampleObject(
                                    name = "default",
                                    summary = "Gültige Anfrage",
                                    value = "{\"email\":\"alice@example.com\",\"password\":\"Str0ngP@ssword!\",\"firstName\":\"Alice\",\"lastName\":\"Doe\"}"
                            ),
                            @ExampleObject(
                                    name = "wrong",
                                    summary = "Ungültige Anfrage",
                                    value = "{\"email\":\"alice@example.com\",\"password\":\"Str0ngP@ssword!\",\"firstName\":\"Alice\"}"
                            )
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
            description = """
            Authentifiziert den Benutzer mit E-Mail und Passwort und liefert ein JWT zurück.
            Das JWT wird zusätzlich im Response-Header 'Authorization: Bearer <JWT>' ausgegeben.
            """
    )
    @RequestBody(
            description = "Login-Daten",
            content = @Content(
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = {
                            @ExampleObject(
                                    name = "Standard",
                                    summary = "Gültige Credentials",
                                    value = "{\"email\":\"alice@example.com\",\"password\":\"Str0ngP@ssword!\"}"
                            ),
                            @ExampleObject(
                                    name = "Falsches Passwort",
                                    summary = "Wird 401 zurückgeben",
                                    value = "{\"email\":\"alice@example.com\",\"password\":\"wrongPass123\"}"
                            )
                    }
            )
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Login erfolgreich",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "Erfolgreiche Antwort",
                                    value = """
                    {
                        "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJlbHlzaW9uLXVzZXItc2VydmljZSIsInVwbiI6InZlcmthdWZlckBlbHlzaW9uLmNvbSIsInN1YiI6IjIyNmU5ZjBlLTdhYzgtNGJmNC1iZjVkLTQyNDU3OWY1NzlmOCIsImdyb3VwcyI6WyJVc2VyIiwiU2VsbGVyIl0sImF1ZCI6ImVseXNpb24tcHJvZHVjdC1zZXJ2aWNlIiwiaWF0IjoxNzU2MDU0ODAyLCJleHAiOjE3NTYwNjIwMDIsImp0aSI6IjBhNjA5NTYwLTdlN2QtNGI2ZC1hMmU0LTM1MDY4Njk3YzY2NSJ9.HOfWYxvvdXw1jNInPwEf5y178a2_BQ2jHYzL1Vy22T7GyHaTqLogXNb_PnCeZmpsL5YKFYng_XszU0jmJ-amJ6m6mm9Hw3A_TWrOmK52j9oFCKuZClvYSYRdX1kh0UkDOJPDYoP5-E705pWPPnS_wLn-o_pCqVPfmDUKEAHPXC-FAcr3Vn5z9KK24IoZLx9h76sdJsjwUY3WiNh8C3QbacPdqavZvJOCLz0tMh1hKCovkbSGgGuRugni_bciZq6tjHbF52eaGVpNTkd-DpmMUc9WyIEGwEAA1ZLzm7AlmdZCX8Z2t9MeayI6RHRfIndSHImK7I9VdXkUQg7_xR0jqg"
                    }
                    """
                            )
                    ),
                    headers = {
                            @Header(
                                    name = HttpHeaders.AUTHORIZATION,
                                    description = "JWT im Bearer-Format (auch im Body enthalten)",
                                    required = true
                            )
                    }
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Ungültige Anmededaten",
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "InvalidCredentials",
                                            value = """
                        {
                            "error": "Invalid credentials"
                        }
                        """
                                    )
                            }
                    )
            ),
            @APIResponse(

            )
    })
    public Response login(@Valid LoginRequest request) {
        LOG.info("Login request: " + request.toString());
        try {
            User user = userService.authenticate(request.email, request.password);
            String token = userService.generateJwt(user);
            LOG.info("Login successful");
            return Response.ok(Map.of("token", token))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();
        } catch (IllegalArgumentException e) {
            LOG.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/confirm-email")
    @PermitAll
    @Operation(
            summary = "E-Mail-Bestätigung durchführen mit der API",
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
        LOG.info("Confirm email request: " + token);
        try {
            userService.confirmEmail(token);
            return Response.ok(Map.of("message","E-Mail erfolgreich bestätigt")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", e.getMessage())).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.GONE).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/resend-activation")
    @PermitAll
    @Transactional
    @Operation(
            summary = "Neuen Aktivierungslink anfordern",
            description = "Sendet einen neuen Aktivierungslink an einen nicht aktivierten Account."
    )
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Neuer Token gesendet"),
            @APIResponse(responseCode = "404", description = "User nicht gefunden"),
            @APIResponse(responseCode = "409", description = "Bereits aktiv")
    })
    public Response resendActivation(@QueryParam("email") String email) {
        LOG.info("Resend activation request: " + email);
        try {
            userService.resendActivationToken(email);
            return Response.ok(Map.of("message", "Neuer Aktivierungslink gesendet")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", e.getMessage())).build();
        }
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

    @POST
    @Path("/login-ident")
    @PermitAll
    @Operation(
            summary = "Passwordless Login via Ident-Token nach E-Mail-Bestätigung",
            description = "Tauscht ein bestätigtes Ident-Token einmalig in ein Access-JWT. Gültig z. B. 15 Minuten nach Bestätigung."
    )
    @APIResponses({
            @APIResponse(responseCode = "200", description = "OK",
                    content = @Content(examples = @ExampleObject(value = "{\"token\":\"<jwt>\"}"))),
            @APIResponse(responseCode = "400", description = "Invalid token"),
            @APIResponse(responseCode = "409", description = "Token already used or expired"),
    })
    public Response loginWithIdent(@QueryParam("token") String token) {
        if (token == null || token.isBlank()) {
            throw new WebApplicationException("Token is missing", 400);
        }
        try {
            String jwt = userService.loginWithIdentToken(token);
            return Response.ok(Map.of("token", jwt))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .build();
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Invalid token", 400);
        } catch (IllegalStateException e) {
            // abgelaufen, schon benutzt, nicht aktiviert
            throw new WebApplicationException(e.getMessage(), 409);
        }
    }

}