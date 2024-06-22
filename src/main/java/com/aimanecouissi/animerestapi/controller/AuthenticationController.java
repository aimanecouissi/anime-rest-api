package com.aimanecouissi.animerestapi.controller;

import com.aimanecouissi.animerestapi.payload.dto.LoginDTO;
import com.aimanecouissi.animerestapi.payload.dto.RegisterDTO;
import com.aimanecouissi.animerestapi.payload.response.AuthenticationResponse;
import com.aimanecouissi.animerestapi.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@Tag(name = "Authentication API", description = "Endpoints for user authentication.")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("login")
    @Operation(summary = "Authenticate user", description = "Endpoint to authenticate user and generate JWT token.")
    @ApiResponse(responseCode = "200", description = "Authentication successful.")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginDTO loginDTO) {
        String token = authenticationService.login(loginDTO);
        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("register")
    @Operation(summary = "Register new user", description = "Endpoint to register a new user.")
    @ApiResponse(responseCode = "201", description = "User registration successful.")
    public ResponseEntity<LoginDTO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        LoginDTO loginDTO = authenticationService.register(registerDTO);
        return new ResponseEntity<>(loginDTO, HttpStatus.CREATED);
    }
}
