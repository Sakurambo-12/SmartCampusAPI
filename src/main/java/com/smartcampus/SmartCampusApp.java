package com.smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

// This annotation tells JAX-RS: "all endpoints live under /api/v1"
// So GET /rooms becomes GET /api/v1/rooms
@ApplicationPath("/api/v1")
public class SmartCampusApp extends Application {
    // Empty body is fine — Jersey will auto-scan for @Path classes
}