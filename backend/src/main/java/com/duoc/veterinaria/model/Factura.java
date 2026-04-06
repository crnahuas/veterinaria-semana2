package com.duoc.veterinaria.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facturas")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Column(name = "subtotal_servicios", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalServicios = BigDecimal.ZERO;

    @Column(name = "subtotal_medicamentos", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalMedicamentos = BigDecimal.ZERO;

    @Column(name = "subtotal_adicionales", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalAdicionales = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(length = 500)
    private String observaciones;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<FacturaDetalle> detalles = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Cita getCita() {
        return cita;
    }

    public void setCita(Cita cita) {
        this.cita = cita;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public BigDecimal getSubtotalServicios() {
        return subtotalServicios;
    }

    public void setSubtotalServicios(BigDecimal subtotalServicios) {
        this.subtotalServicios = subtotalServicios;
    }

    public BigDecimal getSubtotalMedicamentos() {
        return subtotalMedicamentos;
    }

    public void setSubtotalMedicamentos(BigDecimal subtotalMedicamentos) {
        this.subtotalMedicamentos = subtotalMedicamentos;
    }

    public BigDecimal getSubtotalAdicionales() {
        return subtotalAdicionales;
    }

    public void setSubtotalAdicionales(BigDecimal subtotalAdicionales) {
        this.subtotalAdicionales = subtotalAdicionales;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<FacturaDetalle> getDetalles() {
        return detalles;
    }

    public void addDetalle(FacturaDetalle detalle) {
        detalle.setFactura(this);
        detalles.add(detalle);
    }
}
