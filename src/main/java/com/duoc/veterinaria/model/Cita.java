package com.duoc.veterinaria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
public class Cita {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;
    
    @Column(nullable = false)
    private LocalDateTime fechaHora;
    
    @Column(nullable = false)
    private String motivo;
    
    @Column(name = "veterinario_asignado", nullable = false)
    private String veterinarioAsignado;
    
    // Constructores
    public Cita() {}
    
    public Cita(Paciente paciente, LocalDateTime fechaHora, String motivo, String veterinarioAsignado) {
        this.paciente = paciente;
        this.fechaHora = fechaHora;
        this.motivo = motivo;
        this.veterinarioAsignado = veterinarioAsignado;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    
    public String getVeterinarioAsignado() { return veterinarioAsignado; }
    public void setVeterinarioAsignado(String veterinarioAsignado) { this.veterinarioAsignado = veterinarioAsignado; }
}