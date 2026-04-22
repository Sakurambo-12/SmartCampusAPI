package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // GET /api/v1/sensors
    // GET /api/v1/sensors?type=CO2  ← optional filter
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {

        List<Sensor> sensorList = store.getSensors().values()
                .stream()
                .collect(Collectors.toList());

        // If ?type=CO2 was provided, filter the list
        if (type != null && !type.isEmpty()) {
            sensorList = sensorList.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        return Response.ok(sensorList).build();
    }

    // GET /api/v1/sensors/{sensorId}
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);

        if (sensor == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Sensor not found\"}")
                    .build();
        }

        return Response.ok(sensor).build();
    }

    // POST /api/v1/sensors
    @POST
    public Response createSensor(Sensor sensor) {

        // Validate that the roomId actually exists — key business rule
        Room room = store.getRoom(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                    "Room with id '" + sensor.getRoomId() + "' does not exist. " +
                            "Cannot register sensor without a valid room."
            );
        }

        // Check sensor ID not already taken
        if (store.getSensor(sensor.getId()) != null) {
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"Sensor with this ID already exists\"}")
                    .build();
        }

        store.addSensor(sensor);

        // Also add this sensor's ID to the room's sensorIds list
        room.getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    // Sub-resource locator — delegates to SensorReadingResource
    // Handles everything under /api/v1/sensors/{sensorId}/readings
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(
            @PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}