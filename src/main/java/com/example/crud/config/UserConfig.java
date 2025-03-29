/*package com.example.crud.config;

import com.example.crud.model.Role;
import com.example.crud.model.User;
import com.example.crud.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

@Configuration
public class UserConfig {
    @Bean
    CommandLineRunner commandLineRunner(UserRepository userRepository) {
        return args -> {
            User user1 = new User("admin", "sadwalther@gmail.com", 25, "admin");
            user1.setRoles(Set.of(new Role("ROLE_USER"), new Role("ROLE_ADMIN")));
            User user2 = new User("qwe", "qwe@gmail.com", 25, "qwe");
            user2.setRoles(Set.of(new Role("ROLE_USER")));
            User user3 = new User("asd", "asd@gmail.com", 25, "asd");
            user3.setRoles(Set.of(new Role("ROLE_USER")));
            User user4 = new User("zxc", "zxc@gmail.com", 25, "zxc");
            user4.setRoles(Set.of(new Role("ROLE_USER")));
            userRepository.saveAll(List.of(user1, user2, user3, user4));
        };
    }
}*/