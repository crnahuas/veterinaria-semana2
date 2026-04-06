package com.duoc.veterinaria.repository;

import com.duoc.veterinaria.model.Factura;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    @EntityGraph(attributePaths = {"paciente", "cita", "detalles"})
    List<Factura> findAllByOrderByFechaEmisionDesc();

    @EntityGraph(attributePaths = {"paciente", "cita", "detalles"})
    List<Factura> findByPacienteIdOrderByFechaEmisionDesc(Long pacienteId);

    @EntityGraph(attributePaths = {"paciente", "cita", "detalles"})
    List<Factura> findByCitaId(Long citaId);

    @Override
    @EntityGraph(attributePaths = {"paciente", "cita", "detalles"})
    Optional<Factura> findById(Long id);
}
