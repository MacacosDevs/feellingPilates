package com.feelingpilates.pagos.caracterizacion;

import com.feelingpilates.pagos.entidad.*;
import org.junit.jupiter.api.*;
import java.time.OffsetDateTime;
import java.util.List;
import static com.feelingpilates.pagos.caracterizacion.PN14Fixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PagoLecturasPN14Test {
    PagoFixture f;
    @BeforeEach void setup(){f=new PagoFixture();}
    @Test void M06_activosNullPasadoFuturoPrimerOrdenYComboAmbasCategorias() {
        Compra sinFecha=compra(f.paquete,null),pasada=compra(f.paquete,null),
            first=compra(f.paquete,null),second=compra(paquete(),null);
        pasada.setFechaExpiracion(FECHA);first.setFechaExpiracion(OffsetDateTime.now().plusYears(2));
        second.setFechaExpiracion(OffsetDateTime.now().plusYears(1));second.getPaquete().setNombre("Segundo");
        when(f.compras.findByUsuarioIdAndEstadoOrderByFechaExpiracionDesc(CLIENTE,Compra.EstadoCompra.pagada))
            .thenReturn(List.of(sinFecha,pasada,first,second));
        var r=f.service.obtenerPaquetesActivos(CLIENTE);
        assertThat(r).hasSize(1);assertThat(r.getFirst().categoria()).isEqualTo("pilates");
        assertThat(r.getFirst().nombre()).isEqualTo("Pilates dummy");
        assertThat(r.getFirst().fechaInicio()).isEqualTo(first.getFechaExpiracion().minusDays(30));
        assertThat(r.getFirst().fechaExpiracion()).isEqualTo(first.getFechaExpiracion());
        f.paquete.setCategoria(Paquete.CategoriaPaquete.combo);
        var combo=f.service.obtenerPaquetesActivos(CLIENTE);
        assertThat(combo).extracting(x->x.categoria()).containsExactly("pilates","bacu_fit");
        assertThat(combo).allSatisfy(x->assertThat(x.fechaExpiracion()).isEqualTo(first.getFechaExpiracion()));
        verify(f.compras,never()).save(any());
    }
    @Test void M06_LEGACY_NOT_TARGET_historialMutableMontoCompraYVigenciaActual() {
        Compra c=compra(f.paquete,null);c.setEstado(Compra.EstadoCompra.pagada);c.setFechaExpiracion(OffsetDateTime.now().plusYears(1));
        when(f.compras.findByUsuarioIdOrderByCreadoEnDesc(CLIENTE)).thenReturn(List.of(c));
        var original=f.service.obtenerHistorialCompras(CLIENTE).getFirst();
        assertThat(original.id()).isEqualTo(COMPRA);assertThat(original.paqueteNombre()).isEqualTo("Pilates dummy");
        assertThat(original.categoria()).isEqualTo("pilates");assertThat(original.estado()).isEqualTo("pagada");
        assertThat(original.creadoEn()).isEqualTo(FECHA);assertThat(original.fechaExpiracion()).isEqualTo(c.getFechaExpiracion());
        f.paquete.setNombre("Catálogo cambiado");f.paquete.setCategoria(Paquete.CategoriaPaquete.bacu_fit);
        f.paquete.setPrecioCentavos(99999);f.paquete.setVigenciaDias(7);
        var changed=f.service.obtenerHistorialCompras(CLIENTE).getFirst();
        assertThat(changed.paqueteNombre()).isEqualTo("Catálogo cambiado");assertThat(changed.categoria()).isEqualTo("bacu_fit");
        assertThat(changed.montoCentavos()).isEqualTo(12345);assertThat(original.montoCentavos()).isEqualTo(12345);
        when(f.compras.findByUsuarioIdAndEstadoOrderByFechaExpiracionDesc(CLIENTE,Compra.EstadoCompra.pagada)).thenReturn(List.of(c));
        assertThat(f.service.obtenerPaquetesActivos(CLIENTE).getFirst().fechaInicio()).isEqualTo(c.getFechaExpiracion().minusDays(7));
        verify(f.compras,never()).save(any());
    }
    @Test void M06_LEGACY_NOT_TARGET_categoriaNullHistorialNPEYActivosVacios() {
        f.paquete.setCategoria(null);Compra c=compra(f.paquete,null);c.setFechaExpiracion(OffsetDateTime.now().plusYears(1));
        when(f.compras.findByUsuarioIdOrderByCreadoEnDesc(CLIENTE)).thenReturn(List.of(c));
        assertThatThrownBy(()->f.service.obtenerHistorialCompras(CLIENTE)).isInstanceOf(NullPointerException.class);
        when(f.compras.findByUsuarioIdAndEstadoOrderByFechaExpiracionDesc(CLIENTE,Compra.EstadoCompra.pagada)).thenReturn(List.of(c));
        assertThat(f.service.obtenerPaquetesActivos(CLIENTE)).isEmpty();verify(f.compras,never()).save(any());
    }
}
