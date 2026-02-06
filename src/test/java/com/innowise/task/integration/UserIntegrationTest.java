package com.innowise.task.integration;

import com.innowise.task.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
@Import(NoSecurityTestConfig.class)
public class UserIntegrationTest {

    private static final String POSTGRES_PASSWORD = UUID.randomUUID().toString();

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword(POSTGRES_PASSWORD);

    @Container
    public static GenericContainer<?> redis = new GenericContainer<>("redis:7")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port + "/api/users";
    }

    @Test
    void testFullUserFlow() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("John");
        userDTO.setSurname("Doe");
        userDTO.setEmail("john.doe@example.com");
        userDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        userDTO.setActive(true);

        ResponseEntity<UserDTO> createResponse = restTemplate.postForEntity(
                baseUrl, userDTO, UserDTO.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UserDTO createdUser = createResponse.getBody();
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();

        ResponseEntity<UserDTO> getResponse = restTemplate.getForEntity(
                baseUrl + "/" + createdUser.getId(), UserDTO.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getName()).isEqualTo("John");
        assertThat(getResponse.getBody().getActive()).isTrue();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<UserDTO> patchActive = restTemplate.exchange(
                baseUrl + "/" + createdUser.getId() + "/active?active=false",
                HttpMethod.PATCH, entity, UserDTO.class);
        assertThat(patchActive.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(patchActive.getBody().getActive()).isFalse();

        ResponseEntity<UserDTO> updateName = restTemplate.exchange(
                baseUrl + "/" + createdUser.getId() + "?name=Jane&surname=Doe",
                HttpMethod.PATCH, entity, UserDTO.class);
        assertThat(updateName.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateName.getBody().getName()).isEqualTo("Jane");
        assertThat(updateName.getBody().getSurname()).isEqualTo("Doe");

        ResponseEntity<UserDTO> deleteResponse = restTemplate.exchange(
                baseUrl + "/" + createdUser.getId(),
                HttpMethod.DELETE, entity, UserDTO.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deleteResponse.getBody().getId()).isEqualTo(createdUser.getId());

        ResponseEntity<String> deletedGet = restTemplate.getForEntity(
                baseUrl + "/" + createdUser.getId(), String.class);
        assertThat(deletedGet.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(deletedGet.getBody()).contains("User not found");
    }
}
