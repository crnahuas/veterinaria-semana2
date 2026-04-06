package com.duoc.veterinaria.service;

import com.duoc.veterinaria.dto.factura.DetalleFacturaRequest;
import com.duoc.veterinaria.dto.factura.DetalleFacturaResponse;
import com.duoc.veterinaria.dto.factura.FacturaRequest;
import com.duoc.veterinaria.dto.factura.FacturaResponse;
import com.duoc.veterinaria.model.Cita;
import com.duoc.veterinaria.model.Factura;
import com.duoc.veterinaria.model.FacturaDetalle;
import com.duoc.veterinaria.model.Paciente;
import com.duoc.veterinaria.repository.CitaRepository;
import com.duoc.veterinaria.repository.FacturaRepository;
import com.duoc.veterinaria.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FacturaService {

    private static final String TIPO_SERVICIO = "SERVICIO";
    private static final String TIPO_MEDICAMENTO = "MEDICAMENTO";
    private static final String TIPO_ADICIONAL = "ADICIONAL";

    private final FacturaRepository facturaRepository;
    private final PacienteRepository pacienteRepository;
    private final CitaRepository citaRepository;

    public FacturaService(
            FacturaRepository facturaRepository,
            PacienteRepository pacienteRepository,
            CitaRepository citaRepository
    ) {
        this.facturaRepository = facturaRepository;
        this.pacienteRepository = pacienteRepository;
        this.citaRepository = citaRepository;
    }

    @Transactional(readOnly = true)
    public List<FacturaResponse> listar(Long pacienteId) {
        List<Factura> facturas = pacienteId == null
                ? facturaRepository.findAllByOrderByFechaEmisionDesc()
                : facturaRepository.findByPacienteIdOrderByFechaEmisionDesc(pacienteId);

        return facturas.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FacturaResponse obtener(Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        return toResponse(factura);
    }

    @Transactional
    public FacturaResponse generar(FacturaRequest request) {
        Paciente paciente = pacienteRepository.findById(request.getPacienteId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));

        Cita cita = citaRepository.findById(request.getCitaId())
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (!cita.getPaciente().getId().equals(paciente.getId())) {
            throw new IllegalArgumentException("La cita no pertenece al paciente indicado");
        }

        if (request.getServicios().isEmpty()
                && request.getMedicamentos().isEmpty()
                && request.getCargosAdicionales().isEmpty()) {
            throw new IllegalArgumentException("La factura debe incluir al menos un detalle");
        }

        Factura factura = new Factura();
        factura.setPaciente(paciente);
        factura.setCita(cita);
        factura.setFechaEmision(LocalDateTime.now());
        factura.setObservaciones(normalize(request.getObservaciones()));

        BigDecimal subtotalServicios = agregarDetalles(factura, request.getServicios(), TIPO_SERVICIO);
        BigDecimal subtotalMedicamentos = agregarDetalles(factura, request.getMedicamentos(), TIPO_MEDICAMENTO);
        BigDecimal subtotalAdicionales = agregarDetalles(factura, request.getCargosAdicionales(), TIPO_ADICIONAL);
        BigDecimal total = subtotalServicios.add(subtotalMedicamentos).add(subtotalAdicionales);

        factura.setSubtotalServicios(subtotalServicios);
        factura.setSubtotalMedicamentos(subtotalMedicamentos);
        factura.setSubtotalAdicionales(subtotalAdicionales);
        factura.setTotal(total);

        Factura saved = facturaRepository.save(factura);
        return toResponse(saved);
    }

    private BigDecimal agregarDetalles(Factura factura, List<DetalleFacturaRequest> detalles, String tipo) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (DetalleFacturaRequest detalleRequest : detalles) {
            BigDecimal subtotalLinea = detalleRequest.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(detalleRequest.getCantidad()))
                    .setScale(2, RoundingMode.HALF_UP);

            FacturaDetalle detalle = new FacturaDetalle();
            detalle.setTipo(tipo);
            detalle.setDescripcion(detalleRequest.getDescripcion().trim());
            detalle.setCantidad(detalleRequest.getCantidad());
            detalle.setPrecioUnitario(detalleRequest.getPrecioUnitario().setScale(2, RoundingMode.HALF_UP));
            detalle.setSubtotal(subtotalLinea);
            factura.addDetalle(detalle);

            subtotal = subtotal.add(subtotalLinea);
        }

        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    private FacturaResponse toResponse(Factura factura) {
        List<DetalleFacturaResponse> servicios = filtrarDetallesPorTipo(factura, TIPO_SERVICIO);
        List<DetalleFacturaResponse> medicamentos = filtrarDetallesPorTipo(factura, TIPO_MEDICAMENTO);
        List<DetalleFacturaResponse> cargosAdicionales = filtrarDetallesPorTipo(factura, TIPO_ADICIONAL);

        return new FacturaResponse(
                factura.getId(),
                factura.getPaciente().getId(),
                factura.getPaciente().getNombre(),
                factura.getCita().getId(),
                factura.getCita().getMotivo(),
                factura.getFechaEmision(),
                servicios,
                medicamentos,
                cargosAdicionales,
                factura.getSubtotalServicios(),
                factura.getSubtotalMedicamentos(),
                factura.getSubtotalAdicionales(),
                factura.getTotal(),
                factura.getObservaciones()
        );
    }

    private List<DetalleFacturaResponse> filtrarDetallesPorTipo(Factura factura, String tipo) {
        return factura.getDetalles().stream()
                .filter(detalle -> tipo.equals(detalle.getTipo()))
                .map(detalle -> new DetalleFacturaResponse(
                        detalle.getTipo(),
                        detalle.getDescripcion(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        detalle.getSubtotal()
                ))
                .toList();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
