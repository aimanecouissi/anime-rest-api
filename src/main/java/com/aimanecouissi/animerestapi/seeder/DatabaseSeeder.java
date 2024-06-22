package com.aimanecouissi.animerestapi.seeder;

import com.aimanecouissi.animerestapi.entity.*;
import com.aimanecouissi.animerestapi.enums.AnimeStatus;
import com.aimanecouissi.animerestapi.enums.AnimeType;
import com.aimanecouissi.animerestapi.enums.MangaStatus;
import com.aimanecouissi.animerestapi.exception.ResourceNotFoundException;
import com.aimanecouissi.animerestapi.repository.*;
import com.aimanecouissi.animerestapi.utility.ApplicationConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DatabaseSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StudioRepository studioRepository;
    private final AnimeRepository animeRepository;
    private final MangaRepository mangaRepository;

    @Value("${app.admin.first-name}")
    private String ADMIN_FIRST_NAME;

    @Value("${app.admin.last-name}")
    private String ADMIN_LAST_NAME;

    @Value("${app.admin.username}")
    private String ADMIN_USERNAME;

    @Value("${app.admin.password}")
    private String ADMIN_PASSWORD;

    public DatabaseSeeder(
            UserRepository userRepository,
            RoleRepository roleRepository,
            BCryptPasswordEncoder passwordEncoder,
            StudioRepository studioRepository,
            AnimeRepository animeRepository,
            MangaRepository mangaRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.studioRepository = studioRepository;
        this.animeRepository = animeRepository;
        this.mangaRepository = mangaRepository;
    }

    @Override
    public void run(String... args) {
        seedRoles();
        seedUsers();
    }

    private void seedRoles() {
        Role adminRole = Role.builder()
                .name("ROLE_ADMIN")
                .build();
        Role userRole = Role.builder()
                .name("ROLE_USER")
                .build();
        if (roleRepository.findByName(adminRole.getName()).isEmpty()) {
            roleRepository.save(adminRole);
        }
        if (roleRepository.findByName(userRole.getName()).isEmpty()) {
            roleRepository.save(userRole);
        }
    }

    private void seedUsers() {
        if (userRepository.findByUsername(ADMIN_USERNAME).isEmpty()) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElse(null);
            Role userRole = roleRepository.findByName("ROLE_USER").orElse(null);
            assert adminRole != null;
            assert userRole != null;
            User admin = User.builder()
                    .firstName(ADMIN_FIRST_NAME)
                    .lastName(ADMIN_LAST_NAME)
                    .username(ADMIN_USERNAME)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .roles(new HashSet<>(Set.of(adminRole, userRole)))
                    .build();
            userRepository.save(admin);
        }
    }

    private void seedStudios() {
        saveStudioIfNotExist("Studio Ghibli");
        saveStudioIfNotExist("Madhouse");
        saveStudioIfNotExist("Toei Animation");
        saveStudioIfNotExist("Bones");
    }

    private void saveStudioIfNotExist(String name) {
        if (studioRepository.findByName(name).isEmpty()) {
            Studio studio = Studio.builder().name(name).build();
            studioRepository.save(studio);
        }
    }

    private void seedAnime() {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", "admin"));
        User normalUser = userRepository.findByUsername("user")
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", "user"));
        Studio studio1 = studioRepository.findByName("Studio Ghibli")
                .orElseThrow(() -> new ResourceNotFoundException("Studio", "name", "Studio Ghibli"));
        Studio studio2 = studioRepository.findByName("Madhouse")
                .orElseThrow(() -> new ResourceNotFoundException("Studio", "name", "Madhouse"));
        Studio studio3 = studioRepository.findByName("Toei Animation")
                .orElseThrow(() -> new ResourceNotFoundException("Studio", "name", "Toei Animation"));
        Studio studio4 = studioRepository.findByName("Bones")
                .orElseThrow(() -> new ResourceNotFoundException("Studio", "name", "Bones"));

        saveAnimeIfNotExist("Spirited Away", AnimeType.MOVIE, AnimeStatus.COMPLETED, 9, true, true, studio1, adminUser);
        saveAnimeIfNotExist("My Neighbor Totoro", AnimeType.MOVIE, AnimeStatus.WATCHING, 8, false, true, studio2, normalUser);
        saveAnimeIfNotExist("Death Note", AnimeType.TV, AnimeStatus.WATCHING, 8, false, false, studio3, adminUser);
        saveAnimeIfNotExist("One Punch Man", AnimeType.TV, AnimeStatus.PLAN_TO_WATCH, 9, true, true, studio4, normalUser);
    }

    private void saveAnimeIfNotExist(String title, AnimeType type, AnimeStatus status, int rating, boolean favorite, boolean complete, Studio studio, User user) {
        if (animeRepository.findByTitle(title).isEmpty()) {
            Anime anime = Anime.builder()
                    .title(title)
                    .type(type)
                    .status(status)
                    .rating(rating)
                    .isFavorite(favorite)
                    .isComplete(complete)
                    .studio(studio)
                    .user(user)
                    .build();
            animeRepository.save(anime);
        }
    }

    private void seedManga() {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", "admin"));
        User normalUser = userRepository.findByUsername("user")
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", "user"));
        saveMangaIfNotExist("One Piece", MangaStatus.READING, 9, false, adminUser);
        saveMangaIfNotExist("Naruto", MangaStatus.COMPLETED, 8, true, normalUser);
    }

    private void saveMangaIfNotExist(String title, MangaStatus status, int rating, boolean favorite, User user) {
        if (mangaRepository.findByTitle(title).isEmpty()) {
            Manga manga = Manga.builder()
                    .title(title)
                    .status(status)
                    .rating(rating)
                    .isFavorite(favorite)
                    .user(user)
                    .build();
            mangaRepository.save(manga);
        }
    }
}
