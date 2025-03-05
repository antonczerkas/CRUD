package com.example.crud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/")
    public String getHomePage() {
        return "login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}