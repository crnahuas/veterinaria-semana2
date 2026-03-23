package com.duoc.veterinaria.controller;

import com.duoc.veterinaria.model.Paciente;
import com.duoc.veterinaria.repository.PacienteRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    @Autowired
    private PacienteRepository pacienteRepository;

    @GetMapping
    public List<Paciente> listar() {
        return pacienteRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Paciente paciente) {
        try {
            Paciente saved = pacienteRepository.save(paciente);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al registrar paciente: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        return pacienteRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
