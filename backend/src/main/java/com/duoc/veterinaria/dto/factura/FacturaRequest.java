package com.duoc.veterinaria.dto.factura;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class FacturaRequest {

    @NotNull
    private Long pacienteId;

    @NotNull
    private Long citaId;

    @Valid
    private List<DetalleFacturaRequest> servicios = new ArrayList<>();

    @Valid
    private List<DetalleFacturaRequest> medicamentos = new ArrayList<>();

    @Valid
    private List<DetalleFacturaRequest> cargosAdicionales = new ArrayList<>();

    @Size(max = 500)
    private String observaciones;

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public Long getCitaId() {
        return citaId;
    }

    public void setCitaId(Long citaId) {
        this.citaId = citaId;
    }

    public List<DetalleFacturaRequest> getServicios() {
        return servicios;
    }

    public void setServicios(List<DetalleFacturaRequest> servicios) {
        this.servicios = servicios == null ? new ArrayList<>() : servicios;
    }

    public List<DetalleFacturaRequest> getMedicamentos() {
        return medicamentos;
    }

    public void setMedicamentos(List<DetalleFacturaRequest> medicamentos) {
        this.medicamentos = medicamentos == null ? new ArrayList<>() : medicamentos;
    }

    public List<DetalleFacturaRequest> getCargosAdicionales() {
        return cargosAdicionales;
    }

    public void setCargosAdicionales(List<DetalleFacturaRequest> cargosAdicionales) {
        this.cargosAdicionales = cargosAdicionales == null ? new ArrayList<>() : cargosAdicionales;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
