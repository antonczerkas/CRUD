package com.example.crud.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long id;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "age")
    private int age;

    @Column(name = "telegram_chat_id")
    private Long telegramChatId;

    @Column(name = "ruvds_api_token")
    private String ruvdsApiToken;

    @Column(name = "min_balance_threshold")
    private Double minBalanceThreshold;

    @Column(name = "notification_enabled")
    private Boolean notificationEnabled;

    @Column
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "userid"),
            inverseJoinColumns = @JoinColumn(name = "userroleid")
    )
    private Set<Role> roles;

    public User() {
        roles = new HashSet<>();
        roles.add(new Role("ROLE_USER"));
    }

    public User(String name, String email, int age, String password) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.password = password;
        roles = new HashSet<>();
        roles.add(new Role("ROLE_USER"));
    }

    public User(String name, String email, int age, String password, String ruvdsApiToken, Long telegramChatId, Double minBalanceThreshold, Boolean notificationEnabled) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.password = password;
        roles = new HashSet<>();
        roles.add(new Role("ROLE_USER"));
        this.ruvdsApiToken = ruvdsApiToken;
        this.telegramChatId = telegramChatId;
        this.minBalanceThreshold = minBalanceThreshold;
        this.notificationEnabled = notificationEnabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", telegramChatId=" + telegramChatId + '\'' +
                ", ruvdsApiToken='" + ruvdsApiToken +
                ", minBalanceThreshold=" + minBalanceThreshold +
                ", notificationEnabled=" + notificationEnabled +
                ", roles=" + roles +
                '}';
    }
}