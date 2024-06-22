package com.aimanecouissi.animerestapi.integration;

import com.aimanecouissi.animerestapi.entity.Role;
import com.aimanecouissi.animerestapi.repository.RoleRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(OrderAnnotation.class)
class RoleRepositoryIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    private Role role;

    @BeforeEach
    void setUp() {
        roleRepository.deleteAll();
        role = Role.builder().name("ROLE_USER").build();
        roleRepository.save(role);
    }

    @Test
    @Order(1)
    @DisplayName("Save Role")
    void shouldSaveRole() {
        Role newRole = Role.builder()
                .name("ROLE_ADMIN")
                .build();
        Role savedRole = roleRepository.save(newRole);
        assertThat(savedRole).isNotNull();
        assertThat(savedRole.getId()).isGreaterThan(0);
        assertThat(savedRole).usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(newRole);
    }

    @Test
    @Order(2)
    @DisplayName("Find Role By Name")
    void shouldFindRoleByName() {
        Optional<Role> foundRole = roleRepository.findByName(role.getName());
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get()).usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(role);
    }
}
