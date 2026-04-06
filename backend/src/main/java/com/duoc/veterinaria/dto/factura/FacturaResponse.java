package com.duoc.veterinaria.dto.factura;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FacturaResponse(
        Long id,
        Long pacienteId,
        String pacienteNombre,
        Long citaId,
        String motivoCita,
        LocalDateTime fechaEmision,
        List<DetalleFacturaResponse> servicios,
        List<DetalleFacturaResponse> medicamentos,
        List<DetalleFacturaResponse> cargosAdicionales,
        BigDecimal subtotalServicios,
        BigDecimal subtotalMedicamentos,
        BigDecimal subtotalAdicionales,
        BigDecimal total,
        String observaciones
) {
}
