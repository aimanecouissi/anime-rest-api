package com.aimanecouissi.animerestapi.repository;

import com.aimanecouissi.animerestapi.entity.Role;
import com.aimanecouissi.animerestapi.entity.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestMethodOrder(OrderAnnotation.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User user;

    @BeforeEach
    void setUp() {
        // create a role for user
        Role role = Role.builder()
                .name("ROLE_USER")
                .build();
        roleRepository.save(role);

        // Create a user
        user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .password("password123")
                .roles(Set.of(role))
                .build();
        userRepository.save(user);
    }

    @Test
    @Order(1)
    @DisplayName("Save User")
    void shouldSaveUser() {
        User newUser = User.builder()
                .firstName("Bob")
                .lastName("Johnson")
                .username("bob.johnson")
                .password("123password")
                .build();
        User savedUser = userRepository.save(newUser);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isGreaterThan(0);
        assertThat(savedUser).usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(newUser);
    }

    @Test
    @Order(2)
    @DisplayName("Find User By Username")
    void shouldFindUserByUsername() {
        Optional<User> foundUser = userRepository.findByUsername(user.getUsername());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get()).usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(user);
    }

    @Test
    @Order(3)
    @DisplayName("Check User Existence By Username")
    void shouldCheckUserExistenceByUsername() {
        boolean exists = userRepository.existsByUsername(user.getUsername());
        assertThat(exists).isTrue();
    }
}
