package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    // The one and only instance — created when the class is first loaded
    private static final DataStore INSTANCE = new DataStore();

    // ConcurrentHashMap instead of HashMap because multiple requests
    // can arrive simultaneously (threads). ConcurrentHashMap is thread-safe.
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    // Each sensor has its own list of readings
    // Key = sensorId, Value = list of readings for that sensor
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    // Private constructor — nobody can do "new DataStore()" from outside
    private DataStore() {
        seedData(); // Add some sample data so the API isn't empty
    }

    // The only way to get the DataStore
    public static DataStore getInstance() {
        return INSTANCE;
    }

    // --- Room operations ---
    public Map<String, Room> getRooms() { return rooms; }

    public Room getRoom(String id) { return rooms.get(id); }

    public void addRoom(Room room) { rooms.put(room.getId(), room); }

    public boolean deleteRoom(String id) {
        if (rooms.containsKey(id)) {
            rooms.remove(id);
            return true;
        }
        return false;
    }

    // --- Sensor operations ---
    public Map<String, Sensor> getSensors() { return sensors; }

    public Sensor getSensor(String id) { return sensors.get(id); }

    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        // Also initialize an empty readings list for this sensor
        sensorReadings.put(sensor.getId(), new ArrayList<>());
    }

    // --- Reading operations ---
    public List<SensorReading> getReadings(String sensorId) {
        return sensorReadings.getOrDefault(sensorId, new ArrayList<>());
    }

    public void addReading(String sensorId, SensorReading reading) {
        sensorReadings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }

    // --- Seed some initial data so Postman tests have something to work with ---
    private void seedData() {
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Science Lab", 30);
        addRoom(r1);
        addRoom(r2);

        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001", "CO2", "ACTIVE", 400.0, "LAB-101");
        Sensor s3 = new Sensor("OCC-001", "Occupancy", "MAINTENANCE", 0.0, "LIB-301");

        addSensor(s1);
        addSensor(s2);
        addSensor(s3);

        // Link sensors to rooms
        r1.getSensorIds().add("TEMP-001");
        r1.getSensorIds().add("OCC-001");
        r2.getSensorIds().add("CO2-001");
    }
}