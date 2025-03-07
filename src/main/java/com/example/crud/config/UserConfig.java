package com.example.crud.config;

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
            User user1 = new User("Anton", "Cherkas", 25, "sadwalther@gmail.com", "qwe", "qwe123");
            user1.setRoles(Set.of(new Role("ROLE_USER"), new Role("ROLE_ADMIN")));
            User user2 = new User("A", "Sd", 25, "asd@gmail.com", "asd", "asd123");
            user2.setRoles(Set.of(new Role("ROLE_USER")));
            User user3 = new User("Z", "Xc", 25, "zxc@gmail.com", "zxc", "zxc123");
            user3.setRoles(Set.of(new Role("ROLE_USER")));
            User user4 = new User("R", "Ty", 25, "rty@gmail.com", "rty", "rty123");
            user4.setRoles(Set.of(new Role("ROLE_USER")));
            User user5 = new User("D", "Fg", 25, "fgh@gmail.com", "fgh", "fgh123");
            user5.setRoles(Set.of(new Role("ROLE_USER")));
            userRepository.saveAll(List.of(user1, user2, user3, user4, user5));
        };
    }
}