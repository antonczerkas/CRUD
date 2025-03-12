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
            User user1 = new User("qwe", "sadwalther@gmail.com", 25, "qwe");
            user1.setRoles(Set.of(new Role("ROLE_USER"), new Role("ROLE_ADMIN")));
            User user2 = new User("asd", "asd@gmail.com", 25, "asd");
            user2.setRoles(Set.of(new Role("ROLE_USER"), new Role("ROLE_ADMIN")));
            User user3 = new User("zxc", "zxc@gmail.com", 25, "zxc");
            user3.setRoles(Set.of(new Role("ROLE_USER")));
            User user4 = new User("rty", "rty@gmail.com", 25, "rty");
            user4.setRoles(Set.of(new Role("ROLE_USER")));
            User user5 = new User("fgh", "fgh@gmail.com", 25, "fgh");
            user5.setRoles(Set.of(new Role("ROLE_USER")));
            userRepository.saveAll(List.of(user1, user2, user3, user4, user5));
        };
    }
}