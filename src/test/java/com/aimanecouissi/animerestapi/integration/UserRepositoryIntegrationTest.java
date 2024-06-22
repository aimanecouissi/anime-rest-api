package com.aimanecouissi.animerestapi.integration;

import com.aimanecouissi.animerestapi.entity.Role;
import com.aimanecouissi.animerestapi.entity.User;
import com.aimanecouissi.animerestapi.repository.RoleRepository;
import com.aimanecouissi.animerestapi.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(OrderAnnotation.class)
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User user;
    private static Role role;

    @BeforeAll
    static void init(@Autowired RoleRepository roleRepository) {
        roleRepository.deleteAll();
        role = Role.builder().name("ROLE_USER").build();
        roleRepository.save(role);
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        user = User.builder()
                .firstName("Alice")
                .lastName("Smith")
                .username("alice.smith")
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
                .password("password123")
                .roles(Set.of(role))
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
