# ANIME REST API

The Anime REST API is a Spring Boot-based application designed for managing anime and manga collections, featuring comprehensive CRUD operations and admin functionalities for anime studios. Integrated with MySQL and secured with JWT authentication, it ensures secure and efficient data management. This API is ideal for developers who want to test their applications, work with a reliable backend, and learn how to integrate it into front-end applications. The API is easily deployable with Docker, tested with Mockito and JUnit, and includes a Postman collection for testing. It is also deployed on AWS for scalability and reliability.

## INSTALLATION

### PREREQUISITES
- Docker (for running with Docker Compose)
- MySQL (for running manually)

### DOCKER SETUP
1. Clone the repository:
   ```sh
   git clone https://github.com/aimanecouissi/anime-rest-api.git
   cd anime-rest-api
   ```
3. Run the Docker Compose file: `docker-compose up`
5. Set environment variables as needed to run the application locally.

### MANUAL SETUP
1. Clone the repository:
   ```sh
   git clone https://github.com/aimanecouissi/anime-rest-api.git
   cd anime-rest-api
   ```
3. Install MySQL locally and create a database.
4. Configure the application with your MySQL credentials in `src/main/resources/application-development.properties` and `src/main/resources/application-testing.properties`.
5. Set credentials for the admin user to enable CRUD operations on anime studios.

### RUNNING THE APPLICATION
To run the application locally, use the following command: `./mvnw spring-boot`

## USAGE

**Note:** This API has Swagger documentation enabled. You can access the Swagger UI to explore and test the API endpoints interactively at `/swagger-ui/index.html`.

### AUTHENTICATION

#### REGISTER

**Endpoint:** `POST /api/v1/auth/register`

**Description:** Register a new user.

**Parameters:**

| Name      | Type   | In   | Required | Description                 |
|-----------|--------|------|----------|-----------------------------|
| firstName | string | body | Yes      | First name of the user      |
| lastName  | string | body | Yes      | Last name of the user       |
| username  | string | body | Yes      | Username for the user       |
| password  | string | body | Yes      | Password for the user       |

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "username": "john.doe123",
  "password": "Password123"
}
```

**Status Codes:**

| Status Code   | Description                  |
|---------------|------------------------------|
| 201 Created   | User successfully registered |
| 400 Bad Request | Invalid request parameters  |

**Response:**
```json
{
  "username": "john_doe",
  "password": "P@ssw0rd"
}
```

#### LOGIN

**Endpoint:** `POST /api/v1/auth/login`

**Description:** Authenticate a user and retrieve an access token.

**Parameters:**

| Name     | Type   | In   | Required | Description                 |
|----------|--------|------|----------|-----------------------------|
| username | string | body | Yes      | Username of the user        |
| password | string | body | Yes      | Password of the user        |

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "P@ssw0rd"
}
```

**Status Codes:**

| Status Code   | Description                     |
|---------------|---------------------------------|
| 200 OK        | Successful authentication       |
| 400 Bad Request | Invalid request parameters     |

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsIn...",
  "tokenType": "Bearer"
}
```

### STUDIOS MANAGEMENT

#### CREATE STUDIO

**Endpoint:** `POST /api/v1/studios`

**Description:** Add a new studio. Requires admin privileges.

**Parameters:**

| Name | Type   | In   | Required | Description         |
|------|--------|------|----------|---------------------|
| name | string | body | Yes      | Name of the studio  |

**Request Body:**
```json
{
  "name": "Studio Ghibli"
}
```

**Status Codes:**

| Status Code   | Description                 |
|---------------|-----------------------------|
| 201 Created   | Studio successfully created |
| 400 Bad Request | Invalid request parameters |

**Response:**
```json
{
  "id": 1,
  "name": "Studio Ghibli"
}
```

#### GET ALL STUDIOS

**Endpoint:** `GET /api/v1/studios`

**Description:** Retrieve all studios.

**Status Codes:**

| Status Code   | Description                     |
|---------------|---------------------------------|
| 200 OK        | List of studios retrieved successfully |

**Response:**
```json
[
  {
    "id": 1,
    "name": "Studio Ghibli"
  },
  {
    "id": 2,
    "name": "Madhouse"
  }
]
```

#### GET STUDIO BY ID

**Endpoint:** `GET /api/v1/studios/{id}`

**Description:** Retrieve a specific studio by ID.

**Parameters:**

| Name | Type | In   | Required | Description    |
|------|------|------|----------|----------------|
| id   | long | path | Yes      | ID of the studio |

**Status Codes:**

| Status Code   | Description                  |
|---------------|------------------------------|
| 200 OK        | Studio details retrieved successfully |
| 404 Not Found | Studio not found             |

**Response:**
```json
{
  "id": 1,
  "name": "Studio Ghibli"
}
```

#### UPDATE STUDIO

**Endpoint:** `PUT /api/v1/studios/{id}`

**Description:** Modify an existing studio. Requires admin privileges.

**Parameters:**

| Name | Type   | In   | Required | Description          |
|------|--------|------|----------|----------------------|
| id   | long   | path | Yes      | ID of the studio     |
| name | string | body | Yes      | New name of the studio |

**Request Body:**
```json
{
  "name": "New Studio Ghibli"
}
```

**Status Codes:**

| Status Code   | Description                 |
|---------------|-----------------------------|
| 200 OK        | Studio updated successfully |
| 400 Bad Request | Invalid request parameters |
| 404 Not Found | Studio not found            |

**Response:**
```json
{
  "id": 1,
  "name": "New Studio Ghibli"
}
```

#### DELETE STUDIO

**Endpoint:** `DELETE /api/v1/studios/{id}`

**Description:** Remove a studio. Requires admin privileges.

**Parameters:**

| Name | Type | In   | Required | Description    |
|------|------|------|----------|----------------|
| id   | long | path | Yes      | ID of the studio |

**Status Codes:**

| Status Code   | Description                 |
|---------------|-----------------------------|
| 200 OK        | Studio deleted successfully |
| 404 Not Found | Studio not found            |

**Response:**
```json
{
  "message": "The studio has been successfully deleted."
}
```

### ANIME MANAGEMENT

#### CREATE ANIME

**Endpoint:** `POST /api/v1/anime`

**Description:** Add a new anime entry.

**Parameters:**

| Name       | Type   | In   | Required | Description                          |
|------------|--------|------|----------|--------------------------------------|
| title      | string | body | Yes      | Title of the anime                   |
| type       | string | body | Yes      | Type of the anime (e.g., TV, Movie)  |
| studioId   | long   | body | Yes      | ID of the associated studio          |
| status     | string | body | Yes      | Status of the anime                  |
| rating     | int    | body | No       | Rating of the anime (1-10)           |
| isFavorite | bool   | body | No       | Indicates if the anime is a favorite |
| isComplete | bool   | body | No       | Indicates if the anime is complete   |

**Request Body:**
```json
{
  "title": "Attack on Titan",
  "type": "TV",
  "studioId": 1,
  "status": "WATCHING",
  "rating": 9,
  "isFavorite": true,
  "isComplete": false
}
```

**Status Codes:**

| Status Code   | Description                 |
|---------------|-----------------------------|
| 201 Created   | Anime successfully created  |
| 400 Bad Request | Invalid request parameters |

**Response:**
```json
{
  "id": 1,
  "title": "Attack on Titan",
  "type": "TV",
  "studioId": 1,
  "status": "WATCHING",
  "rating": 9,
  "isFavorite": true,
  "isComplete": false
}
```

#### GET ALL ANIME

**Endpoint:** `GET /api/v1/anime`

**Description:** Retrieve a list of all anime entries.

**Parameters:**

| Name     | Type   | In    | Required | Description                                     |
|----------|--------|-------|----------|-------------------------------------------------|
| pageNo   | int    | query | No       | Page number for pagination (default is 0)       |
| pageSize | int    | query | No       | Page size for pagination (default is 10)        |
| sortBy   | string | query | No       | Field to sort by (default is "id")              |
| sortDir  | string | query | No       | Sort direction (asc/desc, default is "asc")     |

**Status Codes:**

| Status Code   | Description                        |
|---------------|------------------------------------|
| 200 OK        | List of anime retrieved successfully |
| 400 Bad Request | Invalid request parameters        |

**Response:**
```json
{
  "items": [
    {
    "id": 1,
    "title": "Attack on Titan",
    "type": "TV",
    "studioId": 1,
    "status": "WATCHING",
    "rating": 9,
    "isFavorite": true,
    "isComplete": false
    }
  ],
  "pageNo": 0,
  "pageSize": 10,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

#### GET ANIME BY ID

**Endpoint:** `GET /api/v1/anime/{id}`

**Description:** Retrieve details of an anime by its unique ID.

**Parameters:**

| Name | Type | In   | Required | Description    |
|------|------|------|----------|----------------|
| id   | long | path | Yes      | Unique ID of the anime |

**Status Codes:**

| Status Code   | Description                  |
|---------------|------------------------------|
| 200 OK        | Anime details retrieved successfully |
| 404 Not Found | Anime not found              |

**Response:**
```json
{
  "id": 1,
  "title": "Attack on Titan",
  "type": "TV",
  "studioId": 1,
  "status": "WATCHING",
  "rating": 9,
  "isFavorite": true,
  "isComplete": false
}
```

#### UPDATE ANIME

**Endpoint:** `PUT /api/v1/anime/{id}`

**Description:** Update details of an existing anime by its ID.

**Parameters:**

| Name       | Type   | In   | Required | Description                          |
|------------|--------|------|----------|--------------------------------------|
| id         | long   | path | Yes      | Unique ID of the anime               |
| title      | string | body | Yes      | Title of the anime                   |
| type       | string | body | Yes       | Type of the anime (e.g., TV, Movie)  |
| studioId   | long   | body | Yes      | ID of the associated studio          |
| status     | string | body | Yes       | Status of the anime                  |
| rating     | int    | body | No       | Rating of the anime (1-10)           |
| isFavorite | bool   | body | No       | Indicates if the anime is a favorite |
| isComplete | bool   | body | No       | Indicates if the anime is complete   |

**Request Body:**
```json
{
  "title": "Attack on Titan",
  "type": "TV",
  "studioId": 1,
  "status": "COMPLETED",
  "rating": 10,
  "isFavorite": true,
  "isComplete": true
}
```

**Status Codes:**

| Status Code   | Description                 |
|---------------|-----------------------------|
| 200 OK        | Anime updated successfully  |
| 400 Bad Request | Invalid request parameters |
| 404 Not Found | Anime not found             |

**Response:**
```json
{
  "id": 1,
  "title": "Attack on Titan",
  "type": "TV",
  "studioId": 1,
  "status": "COMPLETED",
  "rating": 10,
  "isFavorite": true,
  "isComplete": true
}
```

#### DELETE ANIME

**Endpoint:** `DELETE /api/v1/anime/{id}`

**Description:** Delete an anime by its unique ID.

**Parameters:**

| Name | Type | In   | Required | Description    |
|------|------|------|----------|----------------|
| id   | long | path | Yes      | Unique ID of the anime |

**Status Codes:**

| Status Code   | Description                 |
|---------------|-----------------------------|
| 200 OK        | Anime deleted successfully  |
| 404 Not Found | Anime not found             |

**Response:**
```json
{
  "message": "The anime has been successfully deleted."
}
```

### MANGA MANAGEMENT

#### CREATE MANGA

**Endpoint:** `POST /api/v1/manga`

**Description:** Add a new manga entry.

**Parameters:**

| Name       | Type   | In   | Required | Description                           |
|------------|--------|------|----------|---------------------------------------|
| title      | string | body | Yes      | Title of the manga                    |
| status     | string | body | Yes      | Status of the manga                   |
| rating     | int    | body | No       | Rating of the manga (1-10)            |
| isFavorite | bool   | body | No       | Indicates if the manga is a favorite  |

**Request Body:**
```json
{
  "title": "One Piece",
  "status": "READING",
  "rating": 10,
  "isFavorite": true
}
```

**Status Codes:**

| Status Code   | Description                 |
|---------------|-----------------------------|
| 201 Created   | Manga successfully created  |
| 400 Bad Request | Invalid request parameters |

**Response:**
```json
{
  "id": 1,
  "title": "One Piece",
  "status": "READING",
  "rating": 10,
  "isFavorite": true
}
```

#### GET ALL MANGA

**Endpoint:** `GET /api/v1/manga`

**Description:** Retrieve a list of all manga entries.

**Parameters:**

| Name     | Type   | In    | Required | Description                                     |
|----------|--------|-------|----------|-------------------------------------------------|
| pageNo   | int    | query | No       | Page number for pagination (default is 0)       |
| pageSize | int    | query | No       | Page size for pagination (default is 10)        |
| sortBy   | string | query | No       | Field to sort by (default is "id")              |
| sortDir  | string | query | No       | Sort direction (asc/desc, default is "asc")     |

**Status Codes:**

| Status Code   | Description                        |
|---------------|------------------------------------|
| 200 OK        | List of manga retrieved successfully |
| 400 Bad Request | Invalid request parameters        |

**Response:**
```json
{
  "items": [
    {
    "id": 1,
    "title": "One Piece",
    "status": "READING",
    "rating": 10,
    "isFavorite": true
    }
  ],
  "pageNo": 0,
  "pageSize": 10,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

#### GET MANGA BY ID

**Endpoint:** `GET /api/v1/manga/{id}`

**Description:** Retrieve details of a manga by its unique ID.

**Parameters:**

| Name | Type | In   | Required | Description    |
|------|------|------|----------|----------------|
| id   | long | path | Yes      | Unique ID of the manga |

**Status Codes:**

| Status Code   | Description                  |
|---------------|------------------------------|
| 200 OK        | Manga details retrieved successfully |
| 404 Not Found | Manga not found              |

**Response:**
```json
{
  "id": 1,
  "title": "One Piece",
  "status": "READING",
  "rating": 10,
  "isFavorite": true
}
```

#### UPDATE MANGA

**Endpoint:** `PUT /api/v1/manga/{id}`

**Description:** Update details of an existing manga by its ID.

**Parameters:**

| Name       | Type   | In   | Required | Description                          |
|------------|--------|------|----------|--------------------------------------|
| id         | long   | path | Yes      | Unique ID of the manga               |
| title      | string | body | Yes      | Title of the manga                   |
| status     | string | body | Yes      | Status of the manga                  |
| rating     | int    | body | No       | Rating of the manga (1-10)           |
| isFavorite | bool   | body | No       | Indicates if the manga is a favorite |

**Request Body:**
```json
{
  "title": "One Piece",
  "status": "COMPLETED",
  "rating": 10,
  "isFavorite": true
}
```

**Status Codes:**

| Status Code   | Description                 |
|---------------|-----------------------------|
| 200 OK        | Manga updated successfully  |
| 400 Bad Request | Invalid request parameters |
| 404 Not Found | Manga not found             |

**Response:**
```json
{
  "id": 1,
  "title": "One Piece",
  "status": "COMPLETED",
  "rating": 10,
  "isFavorite": true
}
```

#### DELETE MANGA

**Endpoint:** `DELETE /api/v1/manga/{id}`

**Description:** Delete a manga by its unique ID.

**Parameters:**

| Name | Type | In   | Required | Description    |
|------|------|------|----------|----------------|
| id   | long | path | Yes      | Unique ID of the manga |

**Status Codes:**

| Status Code   | Description                 |
|---------------|-----------------------------|
| 200 OK        | Manga deleted successfully  |
| 404 Not Found | Manga not found             |

**Response:**
```json
{
  "message": "The manga has been successfully deleted."
}
```

## ERROR HANDLING

### 400 BAD REQUEST

This error occurs when the request parameters are invalid or missing required fields.

**Example:**
```json
{
  "timestamp": "2024-06-22T10:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": {
    "title": "Title is required"
  }
}
```

### 401 UNAUTHORIZED

This error occurs when the request lacks valid authentication credentials.

**Example:**
```json
{
  "timestamp": "2024-06-22T10:00:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

### 404 NOT FOUND

This error occurs when the requested resource could not be found.

**Example:**
```json
{
  "timestamp": "2024-06-22T10:00:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Resource 'Studio' not found with ID: '999999'."
}
```

### 409 CONFLICT

This error occurs when there is a conflict with the current state of the resource, such as a duplicate entry.

**Example:**
```json
{
  "timestamp": "2024-06-22T10:00:00.000+00:00",
  "status": 409,
  "error": "Conflict",
  "message": "'Username' with value 'aimanecouissi' already exists."
}
```

## ENVIRONMENT VARIABLES

To run the Anime REST API, you need to set the following environment variables:

| Variable Name       | Description                                       |
|---------------------|---------------------------------------------------|
| `JWT_SECRET`        | Secret key used for signing JWT tokens.           |
| `JWT_EXPIRATION`    | Expiration time for JWT tokens in milliseconds.   |
| `ADMIN_FIRST_NAME`  | First name of the admin user.                     |
| `ADMIN_LAST_NAME`   | Last name of the admin user.                      |
| `ADMIN_USERNAME`    | Username for the admin user.                      |
| `ADMIN_PASSWORD`    | Password for the admin user.                      |
| `DB_PASSWORD`       | Password for the MySQL database.                  |

**Example:**
```sh
export JWT_SECRET=d7ac66247bd844d9d333035ad276876ff824665b0db8b1ff7f06b9af531fb567
export JWT_EXPIRATION=604800000
export ADMIN_FIRST_NAME=AIMANE
export ADMIN_LAST_NAME=COUISSI
export ADMIN_USERNAME=aimanecouissi
export ADMIN_PASSWORD=password
export DB_PASSWORD=password
```
## TESTING

You can run tests for the Anime REST API using either Postman or the test folder in the project.

### RUNNING TESTS WITH POSTMAN
1. Import the provided Postman collection into your Postman application.
3. Ensure the server is running.
4. Execute the requests in the collection to validate the API endpoints.

### RUNNING TESTS IN THE TEST FOLDER
1. Navigate to the test folder in your project directory: `cd src/test/java/com/aimanecouissi/animerestapi`
3. Run the tests using Maven: `./mvnw test`

This will execute all the unit and integration tests to ensure the API is working as expected.

## CONTRIBUTING

Contributions are welcome! If you'd like to contribute to the project, please follow these steps:

1. Fork the repository.
2. Create a new branch: `git checkout -b feature-branch`
4. Make your changes and commit them:  `git commit -m "Add some feature"`
6. Push to the branch: `git push origin feature-branch`
8. Open a Pull Request.

Please ensure your code follows the project's coding standards and passes all tests.

## LICENSE

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## RESOURCES

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MySQL Documentation](https://dev.mysql.com/doc)
