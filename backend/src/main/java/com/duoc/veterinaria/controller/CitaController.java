package com.duoc.veterinaria.controller;

import com.duoc.veterinaria.model.Cita;
import com.duoc.veterinaria.model.Paciente;
import com.duoc.veterinaria.repository.CitaRepository;
import com.duoc.veterinaria.repository.FacturaRepository;
import com.duoc.veterinaria.repository.PacienteRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    @GetMapping
    public List<Cita> listar() {
        return citaRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody CitaRequest request) {
        try {
            Paciente paciente = pacienteRepository.findById(request.getPacienteId())
                    .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

            Cita cita = new Cita();
            cita.setPaciente(paciente);
            cita.setFechaHora(request.getFechaHora());
            cita.setMotivo(request.getMotivo());
            cita.setVeterinarioAsignado(request.getVeterinarioAsignado());

            Cita saved = citaRepository.save(cita);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al programar cita: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (!citaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        facturaRepository.deleteAll(facturaRepository.findByCitaId(id));
        citaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public static class CitaRequest {
        @NotNull
        private Long pacienteId;

        @NotNull
        @FutureOrPresent
        private LocalDateTime fechaHora;

        @NotBlank
        private String motivo;

        @NotBlank
        private String veterinarioAsignado;

        public Long getPacienteId() { return pacienteId; }
        public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

        public LocalDateTime getFechaHora() { return fechaHora; }
        public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

        public String getMotivo() { return motivo; }
        public void setMotivo(String motivo) { this.motivo = motivo; }

        public String getVeterinarioAsignado() { return veterinarioAsignado; }
        public void setVeterinarioAsignado(String veterinarioAsignado) { this.veterinarioAsignado = veterinarioAsignado; }
    }
}
