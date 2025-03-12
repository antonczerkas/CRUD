package com.example.crud.service;

import com.example.crud.model.User;
import com.example.crud.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImp implements UserService, UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void saveUser(User user) {
        if (user.getId() == 0) {
            if (!isNewUserValid(user)) {
                return;
            }
        } else {
            changeUser(user);
            return;
        }
        userRepository.save(user);
    }

    public void changeUser(User user) {
        Optional<User> optionalUser = userRepository.findById(user.getId());
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        User previousUser = optionalUser.get();
        user.setPassword(previousUser.getPassword());
        userRepository.save(user);
    }

    private boolean isNewUserValid(User user) {
        Optional<User> optionalUser = userRepository.findByEmail(user.getEmail());
        if (optionalUser.isPresent()) {
            throw new UsernameNotFoundException("User not found");
        }
        optionalUser = userRepository.findByName(user.getName());
        if (optionalUser.isPresent()) {
            throw new UsernameNotFoundException("User not found");
        }
        return true;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByName(username).get();
    }

    public User findUserById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        return optionalUser.get();
    }

    @Transactional
    public User getUserByName(String name) {
        return userRepository.findByName(name).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
}