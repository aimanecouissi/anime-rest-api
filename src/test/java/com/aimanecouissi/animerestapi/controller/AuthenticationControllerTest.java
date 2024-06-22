package com.aimanecouissi.animerestapi.controller;

import com.aimanecouissi.animerestapi.exception.UniqueFieldException;
import com.aimanecouissi.animerestapi.payload.dto.LoginDTO;
import com.aimanecouissi.animerestapi.payload.dto.RegisterDTO;
import com.aimanecouissi.animerestapi.payload.response.AuthenticationResponse;
import com.aimanecouissi.animerestapi.security.JwtTokenProvider;
import com.aimanecouissi.animerestapi.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthenticationControllerTest {

    private static final String API_URL = "/api/v1/auth";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginDTO loginDTO;
    private RegisterDTO registerDTO;
    private AuthenticationResponse authResponse;

    @BeforeEach
    void setUp() {
        loginDTO = LoginDTO.builder()
                .username("user")
                .password("password")
                .build();

        registerDTO = RegisterDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .username("john")
                .password("Azerty123&")
                .build();

        authResponse = AuthenticationResponse.builder()
                .accessToken("jwt-token")
                .tokenType("Bearer")
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
        when(authenticationService.login(any(LoginDTO.class))).thenReturn(authResponse.getAccessToken());
        mockMvc.perform(post(API_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(authResponse)));
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
        when(authenticationService.register(any(RegisterDTO.class))).thenReturn(loginDTO);
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
        when(authenticationService.register(any(RegisterDTO.class))).thenThrow(new UniqueFieldException("Username", "john"));
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
                .username("john")
                .password("123")
                .build();
        when(authenticationService.register(any(RegisterDTO.class))).thenThrow(new ConstraintViolationException("Weak password", null));
        performPostRequest("/register", weakPasswordRegisterDTO, 400);
    }
}
