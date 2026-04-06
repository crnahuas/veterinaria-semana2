package com.duoc.veterinaria.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping({"/", "/home"})
    public String home() {
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/pacientes")
    public String pacientes() {
        return "forward:/pacientes.html";
    }

    @GetMapping("/citas")
    public String citas() {
        return "forward:/citas.html";
    }
}
