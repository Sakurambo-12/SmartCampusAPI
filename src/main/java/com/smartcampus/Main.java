package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class Main {

    public static final String BASE_URI = "http://localhost:8080/api/v1/";


    public static void main(String[] args) throws Exception {

        // ResourceConfig tells Jersey which package to scan for @Path classes
        ResourceConfig config = new ResourceConfig()
                .packages("com.smartcampus");  // scans ALL subpackages too

        // Start Grizzly HTTP server
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI), config);

        System.out.println("Smart Campus API running at " + BASE_URI + "api/v1");
        System.out.println("Press ENTER to stop...");
        System.in.read(); // Keeps the server alive until you press Enter

        server.stop();
    }
}