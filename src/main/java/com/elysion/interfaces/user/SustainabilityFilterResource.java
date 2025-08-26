package com.elysion.interfaces.user;

import com.elysion.application.user.SustainabilityFilterService;
import com.elysion.domain.user.SustainabilityFilter;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/filters")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Sustainability Filters", description = "API zum Abrufen aller verfügbaren Nachhaltigkeitsfilter")
public class SustainabilityFilterResource {

    @Inject
    SustainabilityFilterService filterService;

    @GET
    @PermitAll
    @Operation(
            summary = "Alle Nachhaltigkeitsfilter abrufen",
            description = "Gibt eine Liste aller verfügbaren Nachhaltigkeitsfilter zurück."
    )
    @APIResponse(
            responseCode = "200",
            description = "Liste aller Filter",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SustainabilityFilter.class),
                    examples = @ExampleObject(
                            name = "example",
                            value = "[{\"id\":1,\"key\":\"bio\",\"label\":\"Bio/Organic\",\"description\":\"Products made from organic materials without harmful chemicals\"}," +
                                    "{\"id\":2,\"key\":\"ethical-work\",\"label\":\"Ethical Work Enforced\",\"description\":\"Fair wages and safe working conditions guaranteed\"}]"
                    )
            )
    )
    public Response getAllFilters() {
        List<SustainabilityFilter> filters = filterService.getAllFilters();
        return Response.ok(filters).build();
    }
}