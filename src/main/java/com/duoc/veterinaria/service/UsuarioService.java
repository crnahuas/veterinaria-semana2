package com.duoc.veterinaria.service;

import com.duoc.veterinaria.model.Usuario;
import com.duoc.veterinaria.repository.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        try {
            if (usuarioRepository.count() == 0) {
                Usuario recepcionista = new Usuario();
                recepcionista.setUsername("recepcionista");
                recepcionista.setPassword(passwordEncoder.encode("vet123"));
                recepcionista.setRole("RECEPCIONISTA");
                usuarioRepository.save(recepcionista);

                Usuario veterinario = new Usuario();
                veterinario.setUsername("veterinario");
                veterinario.setPassword(passwordEncoder.encode("vet123"));
                veterinario.setRole("VETERINARIO");
                usuarioRepository.save(veterinario);

                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");
                usuarioRepository.save(admin);
            }
        } catch (Exception e) {
            System.out.println("No se pudieron inicializar usuarios: " + e.getMessage());
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario user = usuarioRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        }
        return user;
    }
}
