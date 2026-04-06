package com.duoc.veterinaria.controller;

import com.duoc.veterinaria.model.Usuario;
import com.duoc.veterinaria.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/veterinarios")
    public ResponseEntity<List<VeterinarioResponse>> listarVeterinarios() {
        List<VeterinarioResponse> veterinarios = usuarioRepository.findByRoleOrderByUsernameAsc("VETERINARIO")
                .stream()
                .map(usuario -> new VeterinarioResponse(usuario.getId(), usuario.getUsername()))
                .toList();

        return ResponseEntity.ok(veterinarios);
    }

    public record VeterinarioResponse(Long id, String username) {
    }
}
