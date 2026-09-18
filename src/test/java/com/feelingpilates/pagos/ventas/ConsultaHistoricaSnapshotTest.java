package com.feelingpilates.pagos.ventas;

import com.feelingpilates.pagos.ventas.infraestructura.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static com.feelingpilates.pagos.ventas.PN14Slice2Fixtures.*;

class ConsultaHistoricaSnapshotTest extends PN14Slice2Fixtures.Postgres {
    @Test void T16_historyNoCatalogAccessClienteAisladoYSinFallbackNULL() throws Exception {
        assertThat(consulta.buscar(cliente,propuesta.id())).isEmpty();assertThat(consulta.listar(cliente)).isEmpty();
        freeze(); var original=consulta.buscar(cliente,propuesta.id());
        jdbc.update("delete from paquete_actividad where paquete_id=?",producto);
        jdbc.update("insert into paquete_actividad(paquete_id,tipo_actividad_id,cantidad_clases) values (?,?,99)",producto,actividades.getFirst());
        jdbc.update("update paquete set nombre='mutado99999',precio_centavos=99999,activo=false where id=?",producto);
        jdbc.update("update tipo_actividad set nombre=nombre||'-mutada',activo=false where id in (?,?)",actividades.get(0),actividades.get(1));
        String role="pn14_lector";
        jdbc.execute("create role "+role+" login password 'pn14_dummy'");
        try(var conn=dataSource.getConnection()){jdbc.execute("grant connect on database "+conn.getCatalog()+" to "+role);}
        jdbc.execute("grant usage on schema public to "+role);
        jdbc.execute("grant select on orden_venta,compra,compra_componente_snapshot to "+role);
        var ds=new org.springframework.jdbc.datasource.DriverManagerDataSource(dataSource.getUrl(),role,"pn14_dummy");
        var j=new JdbcTemplate(ds); var restricted=new ConsultaHistoricaSnapshotJdbcAdapter(j);
        // Este rol no tiene permiso sobre catálogo, actividades ni usuarios.
        assertThatThrownBy(() -> j.queryForList("select * from paquete")).isInstanceOf(org.springframework.dao.DataAccessException.class);
        assertThat(restricted.buscar(cliente,propuesta.id())).isEqualTo(original);
        assertThat(restricted.buscar(otroCliente,propuesta.id())).isEmpty();assertThat(restricted.listar(otroCliente)).isEmpty();
        assertThat(original.orElseThrow().lineas().getFirst().nombreProducto()).isEqualTo("Mixto histórico ~NULL");
        assertThat(original.orElseThrow().lineas().getFirst().componentes().getFirst().cantidad()).isEqualTo(4);
        UUID noSnapshot=UUID.randomUUID();insertar(noSnapshot,null,null,99,"mxn",cliente);
        assertThat(restricted.buscar(cliente,noSnapshot)).isEmpty();assertThat(restricted.listar(cliente)).hasSize(1);
    }
    @Test void T16_ordenCongeladoDescIdAscNoEstadoFinancieroComoFallback() {
        freeze();var original=propuesta;UUID nuevo=UUID.randomUUID();insertar(nuevo,null,null,12345,"mxn",cliente);
        propuesta=orden(cliente,null,List.of(nuevo),producto,actividades,12345,RAW);freeze();
        var esperados=List.of(original.id(),propuesta.id()).stream().sorted(Comparator.comparing(UUID::toString)).toList();
        assertThat(consulta.listar(cliente)).extracting(com.feelingpilates.pagos.ventas.aplicacion.CompraHistoricaSnapshot::ordenId).containsExactlyElementsOf(esperados);
        jdbc.update("update compra set estado='fallida' where id=?",nuevo);
        assertThat(consulta.listar(cliente)).hasSize(2);
    }
}
