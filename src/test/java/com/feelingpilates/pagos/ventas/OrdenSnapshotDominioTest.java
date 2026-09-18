package com.feelingpilates.pagos.ventas;

import com.feelingpilates.pagos.ventas.dominio.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static com.feelingpilates.pagos.ventas.PN14Slice2Fixtures.*;

class OrdenSnapshotDominioTest {
    OrdenVenta muestra() { return orden(UUID.randomUUID(),UUID.randomUUID(),List.of(UUID.randomUUID(),UUID.randomUUID()),UUID.randomUUID(),List.of(UUID.randomUUID(),UUID.randomUUID()),12345,RAW); }
    @Test void T01_listasDefensivasYProductoMixtoEntero() {
        var o=muestra(); var c=o.compras().getFirst();
        assertThatThrownBy(() -> o.compras().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> c.componentes().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> c.proveniencia().campos().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThat(c.componentes()).hasSize(2); assertThat(c.componentes()).extracting(CompraComponenteSnapshot::cantidad).containsExactly(4,2);
        assertThat(c.tipoProducto()).isEqualTo(Compra.TipoProducto.PAQUETE);
    }
    @Test void T02_identidadesCantidadesYActividadDuplicadaFalla() {
        var c=muestra().compras().getFirst();var a=c.componentes().getFirst();
        for(int q:List.of(0,-1)) assertThatThrownBy(() -> new CompraComponenteSnapshot(a.id(),a.compraId(),1,a.actividadId(),a.nombreActividad(),q,"v7")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CompraComponenteSnapshot(UUID.randomUUID(),a.compraId(),1,a.actividadId(),"a",1,"v7")).isInstanceOf(IllegalArgumentException.class);
        var duplicado=new CompraComponenteSnapshot(ContenidoSnapshotCanonico.componenteId(c.id(),2),c.id(),2,a.actividadId(),"b",1,"v7");
        assertThatThrownBy(() -> new Compra(c.id(),c.ordenId(),c.clienteId(),1,c.productoFuenteId(),c.nombreProducto(),c.tipoProducto(),c.precioVenta(),c.politica(),List.of(a,duplicado),c.proveniencia(),S,null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Compra(c.id(),c.ordenId(),c.clienteId(),1,c.productoFuenteId(),c.nombreProducto(),c.tipoProducto(),c.precioVenta(),c.politica(),List.of(c.componentes().get(1)),c.proveniencia(),S,null)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void T02_clienteOrdenMonedaLineasDuplicadasYGapsFalla() {
        var o=muestra(); var c=o.compras().getFirst();
        assertThatThrownBy(() -> new OrdenVenta(o.id(),UUID.randomUUID(),o.scopeKey(),o.total(),o.compras(),S,null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OrdenVenta(UUID.randomUUID(),o.clienteId(),o.scopeKey(),o.total(),o.compras(),S,null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OrdenVenta(o.id(),o.clienteId(),o.scopeKey(),new ImporteMonetario(24690,"USD"),o.compras(),S,null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OrdenVenta(o.id(),o.clienteId(),o.scopeKey(),o.total(),List.of(c,c),S,null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OrdenVenta(o.id(),o.clienteId(),o.scopeKey(),o.total(),List.of(o.compras().get(1)),S,null)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void T02_importeISOYOverflowCheckedSinDistribucion() {
        assertThatThrownBy(() -> new ImporteMonetario(-1,"MXN")).isInstanceOf(IllegalArgumentException.class);
        for(String iso:List.of("mxn","ZZZ","MX","XXX")) assertThatThrownBy(() -> new ImporteMonetario(0,iso)).isInstanceOf(IllegalArgumentException.class);
        var o=muestra(); List<Compra> cs=new ArrayList<>();
        for(var c:o.compras()) cs.add(compra(c.id(),o.id(),o.clienteId(),c.numeroLinea(),c.productoFuenteId(),c.componentes().stream().map(CompraComponenteSnapshot::actividadId).toList(),Long.MAX_VALUE,RAW,S));
        assertThatThrownBy(() -> new OrdenVenta(o.id(),o.clienteId(),o.scopeKey(),new ImporteMonetario(Long.MAX_VALUE,"MXN"),cs,S,null)).isInstanceOf(ArithmeticException.class);
    }
    @Test void T03_precioPagado12345IndependienteDe99999SinDobleMultiplicacion() {
        var o=muestra(); Map<String,Object> catalogo=new HashMap<>(Map.of("precio",99999,"cantidades",List.of(99,99)));
        var antes=o.payloadHash(); catalogo.put("precio",1); catalogo.put("cantidades",List.of(0));
        assertThat(o.total().unidadesMinimas()).isEqualTo(24690); assertThat(o.compras()).allSatisfy(c -> assertThat(c.precioVenta().unidadesMinimas()).isEqualTo(12345));
        assertThat(o.compras().getFirst().componentes()).extracting(CompraComponenteSnapshot::cantidad).containsExactly(4,2);
        assertThat(o.payloadHash()).isEqualTo(antes);
    }
}
