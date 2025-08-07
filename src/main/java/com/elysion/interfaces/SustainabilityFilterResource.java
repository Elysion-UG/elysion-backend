package com.elysion.interfaces;

import com.elysion.application.SustainabilityFilterService;
import com.elysion.domain.SustainabilityFilter;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/filters")
@Produces(MediaType.APPLICATION_JSON)
public class SustainabilityFilterResource {

    @Inject
    SustainabilityFilterService filterService;

    @GET
    @PermitAll
    public Response getAllFilters() {
        List<SustainabilityFilter> filters = filterService.getAllFilters();
        return Response.ok(filters).build();
    }
}