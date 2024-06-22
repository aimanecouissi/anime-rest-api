package com.aimanecouissi.animerestapi.service.implementation;

import com.aimanecouissi.animerestapi.entity.Role;
import com.aimanecouissi.animerestapi.entity.User;
import com.aimanecouissi.animerestapi.exception.ResourceNotFoundException;
import com.aimanecouissi.animerestapi.exception.UniqueFieldException;
import com.aimanecouissi.animerestapi.payload.dto.LoginDTO;
import com.aimanecouissi.animerestapi.payload.dto.RegisterDTO;
import com.aimanecouissi.animerestapi.repository.RoleRepository;
import com.aimanecouissi.animerestapi.repository.UserRepository;
import com.aimanecouissi.animerestapi.security.JwtTokenProvider;
import com.aimanecouissi.animerestapi.service.AuthenticationService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthenticationServiceImplementation implements AuthenticationService {
    private static final String ROLE_USER = "ROLE_USER";
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthenticationServiceImplementation(AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public String login(LoginDTO loginDTO) {
        Authentication authentication = authenticateUser(loginDTO.getUsername(), loginDTO.getPassword());
        return jwtTokenProvider.generateToken(authentication);
    }

    @Override
    @Transactional
    public LoginDTO register(RegisterDTO registerDTO) {
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new UniqueFieldException("Username", registerDTO.getUsername());
        }
        User user = createUser(registerDTO);
        userRepository.save(user);
        return LoginDTO.builder()
                .username(registerDTO.getUsername())
                .password(registerDTO.getPassword())
                .build();
    }

    private Authentication authenticateUser(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    private User createUser(RegisterDTO registerDTO) {
        User user = User.builder()
                .firstName(registerDTO.getFirstName())
                .lastName(registerDTO.getLastName())
                .username(registerDTO.getUsername())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .build();
        user.setRoles(getDefaultRoles());
        return user;
    }

    private Set<Role> getDefaultRoles() {
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", ROLE_USER)));
        return roles;
    }
}
