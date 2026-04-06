package com.duoc.veterinaria.dto.factura;

import java.math.BigDecimal;

public record DetalleFacturaResponse(
        String tipo,
        String descripcion,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}
