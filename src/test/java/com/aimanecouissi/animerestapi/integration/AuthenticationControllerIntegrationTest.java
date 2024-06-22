package com.aimanecouissi.animerestapi.integration;

import com.aimanecouissi.animerestapi.entity.Role;
import com.aimanecouissi.animerestapi.entity.User;
import com.aimanecouissi.animerestapi.payload.dto.LoginDTO;
import com.aimanecouissi.animerestapi.payload.dto.RegisterDTO;
import com.aimanecouissi.animerestapi.repository.RoleRepository;
import com.aimanecouissi.animerestapi.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(OrderAnnotation.class)
public class AuthenticationControllerIntegrationTest {

    private static final String API_URL = "/api/v1/auth";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginDTO loginDTO;
    private RegisterDTO registerDTO;

    @BeforeAll
    static void init(@Autowired RoleRepository roleRepository) {
        roleRepository.deleteAll();
        roleRepository.save(Role.builder().name("ROLE_USER").build());
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // User DTO for sign in
        loginDTO = LoginDTO.builder()
                .username("john.doe")
                .password("password123")
                .build();

        // User DTO for sign up
        registerDTO = RegisterDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .password("password123")
                .build();
    }

    private void performPostRequest(String endpoint, Object dto, int expectedStatus) throws Exception {
        mockMvc.perform(post(API_URL + endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    @Order(1)
    @DisplayName("Login - Success")
    void shouldLoginSuccessfully() throws Exception {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .password(passwordEncoder.encode("password123"))
                .build();
        userRepository.save(user);
        mockMvc.perform(post(API_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @Order(2)
    @DisplayName("Login - Missing Fields")
    void shouldThrowExceptionWhenLoginWithMissingFields() throws Exception {
        LoginDTO invalidLoginDTO = LoginDTO.builder().build();
        performPostRequest("/login", invalidLoginDTO, 400);
    }

    @Test
    @Order(3)
    @DisplayName("Register - Success")
    void shouldRegisterSuccessfully() throws Exception {
        mockMvc.perform(post(API_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(loginDTO)));
    }

    @Test
    @Order(4)
    @DisplayName("Register - Duplicate Username")
    void shouldThrowExceptionWhenRegisteringWithDuplicateUsername() throws Exception {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .password(passwordEncoder.encode("password123"))
                .build();
        userRepository.save(user);
        performPostRequest("/register", registerDTO, 409);
    }

    @Test
    @Order(5)
    @DisplayName("Register - Missing Fields")
    void shouldThrowExceptionWhenRegisteringWithMissingFields() throws Exception {
        RegisterDTO invalidRegisterDTO = RegisterDTO.builder().build();
        performPostRequest("/register", invalidRegisterDTO, 400);
    }

    @Test
    @Order(6)
    @DisplayName("Register - Weak Password")
    void shouldThrowExceptionWhenRegisteringWithWeakPassword() throws Exception {
        RegisterDTO weakPasswordRegisterDTO = RegisterDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .password("123456789")
                .build();
        performPostRequest("/register", weakPasswordRegisterDTO, 400);
    }
}
