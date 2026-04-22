package com.smartcampus.resource;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

@Provider  // Jersey auto-registers this as a filter
public class LoggingFilter
        implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER =
            Logger.getLogger(LoggingFilter.class.getName());

    // Runs BEFORE every request hits your resource method
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info("Incoming request: "
                + requestContext.getMethod()        // GET, POST, DELETE etc
                + " "
                + requestContext.getUriInfo().getRequestUri());  // full URL
    }

    // Runs AFTER every response leaves your resource method
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOGGER.info("Outgoing response: status "
                + responseContext.getStatus());     // 200, 201, 404 etc
    }
}