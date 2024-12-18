package com.dangdangsalon.domain.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OAuth2PageController {

    @GetMapping("/custom/login")
    public String oAuth2LoginPage() {
        return "login";
    }
}
