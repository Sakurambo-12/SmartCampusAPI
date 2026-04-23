# SmartCampusAPI

A RESTful API built with JAX-RS (Jersey) for managing university rooms and sensors.

## Technology Stack
- Java 11
- JAX-RS (Jersey 2.41)
- Grizzly HTTP Server (embedded)
- Maven

## How to Build and Run

**Prerequisites:** Java 11+, Maven

```bash
# Clone the repository
git clone https://github.com/Sakurambo-12/SmartCampusAPI.git
cd SmartCampusAPI

# Build
mvn clean package

# Run
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar
```

API will start at: `http://localhost:8080/api/v1/`

## Sample curl Commands

```bash
# 1. Get API discovery info
curl http://localhost:8080/api/v1/

# 2. Get all rooms
curl http://localhost:8080/api/v1/rooms

# 3. Create a new room
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"HALL-01","name":"Main Hall","capacity":200}'

# 4. Get all sensors filtered by type
curl http://localhost:8080/api/v1/sensors?type=CO2

# 5. Add a reading to a sensor
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":23.5}'
```

## Report — Question Answers

### Part 1.1 — Explain the default lifecycle of a JAX-RS Resource class and how it impacts in-memory data management.

By default, JAX-RS creates a brand new instance of a resource class each time a request comes in. This means that if data was stored directly inside a resource class, it would be wiped out after every request. To solve this, all the data is kept in a separate DataStore class that is created once and reused for the entire lifetime of the application. This way, rooms and sensors persist across requests. Since multiple requests can arrive at the same time from different users, ConcurrentHashMap is used instead of a regular HashMap to make sure the two requests do not corrupt the data by writing to it at the same time.

### Part 1.2 — Why is HATEOAS considered a hallmark of advanced RESTful design?

HATEOAS means that instead of just returning raw data, the API also tells the client where it can go next by including links in its responses. For example, the discovery endpoint returns the URLs for rooms and sensors, so a developer using the API does not need to guess or look up the available endpoints. This makes the API easier to explore and use, and if a URL ever changes, clients that follow the links will still work correctly rather than breaking. It essentially makes the API self-describing rather than requiring developers to rely entirely on external documentation.

### Part 2.1 — What are the implications of returning only IDs versus full room objects in a list response?

Returning only IDs keeps the responses small and fast, which matters when there are many rooms. However, the client then has to send a separate request for each room to get the actual details, which adds extra back-and-forth. Returning full room objects means the client gets everything in one go, which is simpler and faster for the client, but the response is larger. For this API, full objects are returned because each room only has a few fields, so the extra size is not a concern. In a much larger system with hundreds of fields per room, returning a shorter summary would be the better approach.

### Part 2.2 — Is DELETE idempotent in your implementation?

Yes, DELETE is idempotent. This means no matter how many times you send the same DELETE request, the end result on the server is the same. The first time you delete a room that exists and has no sensors, it gets removed and the server responds with 204 No Content. If you send the exact same request again, the room is already gone, so the server responds with 404 Not Found. Either way, the room does not exist on the server after the request, which is the expected outcome. The response code changes but the actual state of the server does not, which satisfies the definition of idempotency.

### Part 3.1 — What happens if a client sends data in a format other than JSON to a method annotated with @Consumes(APPLICATION_JSON)?

Jersey automatically rejects the request and sends back a 415 Unsupported Media Type error before the request even reaches the method. This happens because the @Consumes annotation tells Jersey to only accept requests where the Content-Type header says application/json. If a client sends text/plain or application/xml instead, Jersey sees that no method can handle it and rejects it immediately. No extra code needs to be written to handle this, the framework takes care of it entirely.

### Part 3.2 — Why is a query parameter superior to a path parameter for filtering?

Query parameters like GET /api/v1/sensors?type=CO2 are the right choice for filtering because they are completely optional. If you leave out the parameter, you simply get all sensors back. This makes the same endpoint work for both fetching everything and fetching a filtered subset. Path parameters like /api/v1/sensors/type/CO2 suggest that "type/CO2" is its own specific resource that lives at that address, which does not make sense for a filter. Query parameters are also easy to combine, for example ?type=CO2&status=ACTIVE, whereas adding more filters to a path-based approach quickly becomes messy and hard to read.

### Part 4.1 — What are the architectural benefits of the Sub-Resource Locator pattern?

The sub-resource locator pattern lets a resource class hand off responsibility for a nested URL to a completely separate class. In this API, SensorResource deals with everything under /api/v1/sensors, and when a request comes in for /sensors/{id}/readings, it simply creates and returns a SensorReadingResource object, which then handles the rest. This keeps each class focused on one thing, making the code much easier to understand and manage. If everything was written in one giant class, it would quickly become difficult to maintain as the API grows. Thus, splitting responsibilities across classes also makes it easier to update one part without accidentally breaking another.

### Part 5.1 — Why is HTTP 422 more semantically accurate than 404 for a missing referenced resource?

A 404 error means the URL that was requested could not be found. But in this case, the URL /api/v1/sensors is perfectly valid and exists. The problem is not with the URL but with the data inside the request body, specifically a roomId that points to a room that does not exist. HTTP 422 Unprocessable Entity is the right code here because it tells the client that the server understood the request and found the endpoint, but could not act on the data because something inside it was wrong. This gives the client a much clearer signal that they need to fix the content of their request, not the URL they are calling.

### Part 5.2 — What cybersecurity risks come from exposing Java stack traces?

When a Java stack trace is returned to a client, it can reveal a lot of sensitive information about how the application is built. It shows the names of internal classes and packages, which gives attackers a picture of the application's structure. It also shows which libraries and versions are being used, making it easy to look up known security vulnerabilities for those exact versions. File paths and internal logic can also appear in traces, which attackers can use to craft more targeted attacks. The global exception mapper in this API prevents any of this from leaking out by catching all errors internally, logging them on the server for developers to review, and only sending back a plain generic message to the client.

### Part 5.3 — Why use JAX-RS filters for logging instead of inserting Logger.info() in every method?

If logging was added manually to every single resource method, the same lines of code would be repeated dozens of times across the project. This creates unnecessary clutter and means that if the logging format ever needs to change, every method has to be updated individually, which is error-prone. A JAX-RS filter solves this by sitting in front of every request and response automatically. It logs what it needs to without any resource method having to know about it. This keeps the resource classes clean and focused on their actual job, and guarantees that every request gets logged consistently, including any new endpoints added in the future.
