package com.innowise.task.integration;

import com.innowise.task.dto.PaymentCardDTO;
import com.innowise.task.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.UUID;
import java.time.Duration;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
@Import(NoSecurityTestConfig.class)
public class PaymentCardIntegrationTest {

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

    @Autowired
    private StringRedisTemplate redisTemplate;

    private String baseUrl;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port;
    }

    @Test
    void testFullFlowWithRedisCaching() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("John");
        userDTO.setSurname("Doe");
        userDTO.setEmail("john.doe@example.com");
        userDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        userDTO.setActive(true);

        ResponseEntity<UserDTO> userResponse = restTemplate.postForEntity(
                baseUrl + "/api/users", userDTO, UserDTO.class);
        assertThat(userResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UserDTO createdUser = userResponse.getBody();
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();

        PaymentCardDTO cardDTO = new PaymentCardDTO();
        cardDTO.setUserId(createdUser.getId());
        cardDTO.setNumber("1234567812345678");
        cardDTO.setHolder("John Doe");
        cardDTO.setExpirationDate(LocalDate.now().plusYears(2));
        cardDTO.setActive(true);

        ResponseEntity<PaymentCardDTO> cardResponse = restTemplate.postForEntity(
                baseUrl + "/api/cards", cardDTO, PaymentCardDTO.class);
        assertThat(cardResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        PaymentCardDTO createdCard = cardResponse.getBody();
        assertThat(createdCard).isNotNull();
        assertThat(createdCard.getId()).isNotNull();

        String cachedCardKey = "cards::" + createdCard.getId();
        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> assertThat(redisTemplate.hasKey(cachedCardKey)).isTrue());

        ResponseEntity<PaymentCardDTO> getCardResponse = restTemplate.getForEntity(
                baseUrl + "/api/cards/" + createdCard.getId(), PaymentCardDTO.class);
        assertThat(getCardResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getCardResponse.getBody().getNumber()).isEqualTo("1234567812345678");

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<PaymentCardDTO> patchResponse = restTemplate.exchange(
                baseUrl + "/api/cards/" + createdCard.getId() + "/active?active=false",
                HttpMethod.PATCH, entity, PaymentCardDTO.class);
        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(patchResponse.getBody().getActive()).isFalse();

        ResponseEntity<PaymentCardDTO[]> cardsByUser = restTemplate.getForEntity(
                baseUrl + "/api/cards/user/" + createdUser.getId(), PaymentCardDTO[].class);
        assertThat(cardsByUser.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cardsByUser.getBody()).hasSize(1);

        ResponseEntity<PaymentCardDTO> deleteResponse = restTemplate.exchange(
                baseUrl + "/api/cards/" + createdCard.getId(), HttpMethod.DELETE, entity, PaymentCardDTO.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deleteResponse.getBody().getId()).isEqualTo(createdCard.getId());

        ResponseEntity<String> deletedCardResponse = restTemplate.getForEntity(
                baseUrl + "/api/cards/" + createdCard.getId(), String.class);
        assertThat(deletedCardResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(deletedCardResponse.getBody()).contains("Payment card not found");
    }
}
