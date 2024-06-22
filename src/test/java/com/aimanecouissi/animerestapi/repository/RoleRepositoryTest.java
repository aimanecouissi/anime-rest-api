package com.aimanecouissi.animerestapi.repository;

import com.aimanecouissi.animerestapi.entity.Role;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestMethodOrder(OrderAnnotation.class)
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    private Role role;

    @BeforeEach
    void setUp() {
        role = Role.builder()
                .name("ROLE_USER")
                .build();
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
