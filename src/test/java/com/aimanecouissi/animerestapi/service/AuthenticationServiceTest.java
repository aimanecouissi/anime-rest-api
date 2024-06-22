package com.aimanecouissi.animerestapi.service;

import com.aimanecouissi.animerestapi.entity.Role;
import com.aimanecouissi.animerestapi.entity.User;
import com.aimanecouissi.animerestapi.exception.ResourceNotFoundException;
import com.aimanecouissi.animerestapi.exception.UniqueFieldException;
import com.aimanecouissi.animerestapi.payload.dto.LoginDTO;
import com.aimanecouissi.animerestapi.payload.dto.RegisterDTO;
import com.aimanecouissi.animerestapi.repository.RoleRepository;
import com.aimanecouissi.animerestapi.repository.UserRepository;
import com.aimanecouissi.animerestapi.security.JwtTokenProvider;
import com.aimanecouissi.animerestapi.service.implementation.AuthenticationServiceImplementation;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthenticationServiceImplementation authenticationService;

    private User user;
    private Role role;
    private RegisterDTO registerDTO;
    private LoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        // Role for user
        role = Role.builder()
                .name("ROLE_USER")
                .build();

        // User
        user = User.builder()
                .firstName("Alice")
                .lastName("Smith")
                .username("alice.smith")
                .password("password123")
                .roles(Set.of(role))
                .build();

        // Register DTO
        registerDTO = RegisterDTO.builder()
                .firstName("Alice")
                .lastName("Smith")
                .username("alice.smith")
                .password("password123")
                .build();

        // Register BO
        loginDTO = LoginDTO.builder()
                .username("alice.smith")
                .password("password123")
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("Sign in")
    void shouldLoginSuccessfully() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token");
        String token = authenticationService.login(loginDTO);
        assertThat(token).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken(authentication);
    }

    @Test
    @Order(2)
    @DisplayName("Register User - Success")
    void shouldRegisterUserSuccessfully() {
        when(userRepository.existsByUsername(registerDTO.getUsername())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(registerDTO.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        LoginDTO returnedLoginDTO = authenticationService.register(registerDTO);
        assertThat(returnedLoginDTO).isNotNull();
        assertThat(returnedLoginDTO.getUsername()).isEqualTo(registerDTO.getUsername());
        verify(userRepository).existsByUsername(registerDTO.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @Order(3)
    @DisplayName("Register User - Duplicate Username")
    void shouldThrowExceptionWhenRegisteringWithDuplicateUsername() {
        when(userRepository.existsByUsername(registerDTO.getUsername())).thenReturn(true);
        UniqueFieldException exception = assertThrows(UniqueFieldException.class, () -> authenticationService.register(registerDTO));
        assertThat(exception.getMessage()).contains("Username", registerDTO.getUsername());
        verify(userRepository).existsByUsername(registerDTO.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @Order(4)
    @DisplayName("Register User - Role Not Found")
    void shouldThrowExceptionWhenRoleNotFound() {
        when(userRepository.existsByUsername(registerDTO.getUsername())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> authenticationService.register(registerDTO));
        assertThat(exception.getMessage()).contains("Role", "name", "ROLE_USER");
        verify(userRepository).existsByUsername(registerDTO.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }
}
