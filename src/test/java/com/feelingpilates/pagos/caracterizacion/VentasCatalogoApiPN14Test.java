package com.feelingpilates.pagos.caracterizacion;

import com.feelingpilates.exception.*;
import com.feelingpilates.pagos.controlador.*;
import com.feelingpilates.pagos.dto.*;
import com.feelingpilates.pagos.entidad.PaqueteActividad;
import com.feelingpilates.pagos.repositorio.PaqueteRepository;
import com.feelingpilates.pagos.servicio.*;
import com.feelingpilates.seguridad.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.*;
import static com.feelingpilates.pagos.caracterizacion.PN14Fixtures.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({VentaController.class,PaqueteController.class,PaqueteGestionController.class})
@Import({SecurityConfig.class,VentasCatalogoApiPN14Test.Config.class})
class VentasCatalogoApiPN14Test {
    @Autowired MockMvc mvc;@Autowired VentaService ventas;@Autowired PaqueteGestionService gestion;@Autowired PaqueteRepository paquetes;
    @BeforeEach void setup(){reset(ventas,gestion,paquetes);}
    VentaResponse venta(){return new VentaResponse(COMPRA,"Cliente","Paquete",12345,"transferencia","pagada",FECHA,null,"Actor","Sede",COMPRA,1,null);}
    PaqueteGestionResponse paqueteDTO(){return new PaqueteGestionResponse(PAQUETE,"Dummy","Descripción dummy",100,30,"2 clases",true,true,7,List.of(new ActividadPaqueteResponse(actividad().getId(),"Reformer dummy",2)),FECHA);}
    String paqueteJSON(){return "{\"id\":\""+PAQUETE+"\",\"nombre\":\"Dummy\",\"descripcion\":\"Descripción dummy\",\"precioCentavos\":100,\"vigenciaDias\":30,\"unitarioTexto\":\"2 clases\",\"destacado\":true,\"activo\":true,\"orden\":7,\"actividades\":[{\"tipoActividadId\":\""+actividad().getId()+"\",\"nombreActividad\":\"Reformer dummy\",\"cantidadClases\":2}],\"creadoEn\":\"2020-01-02T03:04:05Z\"}";}
    String body(){return "{\"clienteId\":\""+CLIENTE+"\",\"paqueteId\":\""+PAQUETE+"\",\"salonId\":\""+SALON+"\",\"metodoPago\":\"transferencia\"}";}
    String catalogBody(){return "{\"nombre\":\"Dummy\",\"precioCentavos\":100,\"vigenciaDias\":30,\"destacado\":false,\"orden\":0,\"actividades\":[{\"tipoActividadId\":\""+SALON+"\",\"cantidadClases\":2}]}";}
    String carritoBody(){return "{\"clienteId\":\""+CLIENTE+"\",\"salonId\":\""+SALON+"\",\"metodoPago\":\"efectivo\",\"items\":[{\"paqueteId\":\""+PAQUETE+"\",\"cantidad\":1}]}";}
    String ventaJSON(String metodo,String estado,String motivo){return "{\"id\":\""+COMPRA+"\",\"clienteNombre\":\"Cliente\",\"paqueteNombre\":\"Paquete\",\"montoCentavos\":12345,\"metodoPago\":\""+metodo+"\",\"estado\":\""+estado+"\",\"creadoEn\":\"2020-01-02T03:04:05Z\",\"fechaExpiracion\":null,\"registradaPorNombre\":\"Actor\",\"salonNombre\":\"Sede\",\"grupoCompraId\":\""+COMPRA+"\",\"numeroItem\":1,\"motivoEstado\":"+(motivo==null?"null":"\""+motivo+"\"")+"}";}
    @Test void M10_publicoSinJWTYVenta201ContratoCarritoRefundYSedes() throws Exception {
        var p=paquete();p.setCategoria(null);p.setDestacado(true);p.getActividades().add(new PaqueteActividad(p,actividad(),4));when(paquetes.findByActivoTrueOrderByCategoriaAscOrdenAsc()).thenReturn(List.of(p));
        mvc.perform(get("/api/publico/paquetes")).andExpect(status().isOk()).andExpect(jsonPath("$[0].categoria").isEmpty())
            .andExpect(content().json("[{\"id\":\""+PAQUETE+"\",\"categoria\":null,\"nombre\":\"Pilates dummy\",\"descripcion\":\"Descripción dummy\",\"precioCentavos\":12345,\"vigenciaDias\":30,\"unitarioTexto\":\"4 clases\",\"destacado\":true,\"actividades\":[{\"tipoActividadId\":\""+actividad().getId()+"\",\"nombreActividad\":\"Reformer dummy\",\"cantidadClases\":4}]}]",org.springframework.test.json.JsonCompareMode.STRICT));
        when(ventas.registrarVenta(CLIENTE,PAQUETE,SALON,"transferencia",ACTOR)).thenReturn(venta());
        mvc.perform(post("/api/ventas").with(principal("venta.registrar.crear")).contentType("application/json").content(body()))
            .andExpect(status().isCreated()).andExpect(content().json("{\"id\":\""+COMPRA+"\",\"clienteNombre\":\"Cliente\",\"paqueteNombre\":\"Paquete\",\"montoCentavos\":12345,\"metodoPago\":\"transferencia\",\"estado\":\"pagada\",\"creadoEn\":\"2020-01-02T03:04:05Z\",\"fechaExpiracion\":null,\"registradaPorNombre\":\"Actor\",\"salonNombre\":\"Sede\",\"grupoCompraId\":\""+COMPRA+"\",\"numeroItem\":1,\"motivoEstado\":null}",org.springframework.test.json.JsonCompareMode.STRICT));
        verify(ventas).registrarVenta(CLIENTE,PAQUETE,SALON,"transferencia",ACTOR);
        var item=new VentaResponse(COMPRA,"Cliente","Paquete",12345,"efectivo","pagada",FECHA,null,"Actor","Sede",COMPRA,1,null);
        when(ventas.registrarVentaCarrito(eq(CLIENTE),eq(SALON),eq("efectivo"),anyList(),eq(ACTOR))).thenReturn(new VentaCarritoResponse(COMPRA,List.of(item),12345));
        mvc.perform(post("/api/ventas/carrito").with(principal("venta.registrar.crear")).contentType("application/json")
            .content(carritoBody()))
            .andExpect(status().isCreated()).andExpect(content().json("{\"grupoCompraId\":\""+COMPRA+"\",\"items\":["+ventaJSON("efectivo","pagada",null)+"],\"totalCentavos\":12345}",org.springframework.test.json.JsonCompareMode.STRICT));
        verify(ventas).registrarVentaCarrito(CLIENTE,SALON,"efectivo",List.of(new ItemCarritoRequest(PAQUETE,1)),ACTOR);
        var refund=new VentaResponse(COMPRA,"Cliente","Paquete",12345,"transferencia","reembolsada",FECHA,null,"Actor","Sede",COMPRA,1,"dummy motivo");
        when(ventas.reembolsarVenta(COMPRA,"dummy motivo")).thenReturn(refund);
        mvc.perform(patch("/api/ventas/{id}/reembolsar",COMPRA).with(principal("venta.gestion.gestionar")).contentType("application/json").content("{\"motivo\":\"dummy motivo\"}"))
            .andExpect(status().isOk()).andExpect(content().json(ventaJSON("transferencia","reembolsada","dummy motivo"),org.springframework.test.json.JsonCompareMode.STRICT));verify(ventas).reembolsarVenta(COMPRA,"dummy motivo");
        when(ventas.sedesDisponibles(ACTOR)).thenReturn(List.of(new SedeVentaResponse(SALON,"Sede")));
        for(String permiso:List.of("venta.registrar.vista","venta.gestion.vista"))mvc.perform(get("/api/ventas/sedes").with(principal(permiso)))
            .andExpect(status().isOk()).andExpect(content().json("[{\"id\":\""+SALON+"\",\"nombre\":\"Sede\"}]",org.springframework.test.json.JsonCompareMode.STRICT));
        verify(ventas,times(2)).sedesDisponibles(ACTOR);
    }
    @Test void M10_buscarPropioVsTodosYHistorialCompletoPermiso() throws Exception {
        var desde=LocalDate.of(2026,8,1);var hasta=LocalDate.of(2026,8,24);
        var pageable=PageRequest.of(2,3,Sort.by(Sort.Direction.DESC,"creadoEn"));
        when(ventas.historial("transferencia",SALON,"pagada",ACTOR,desde,hasta," Cliente ",pageable)).thenReturn(new PageImpl<>(List.of(venta()),pageable,10));
        when(ventas.historial("transferencia",SALON,"pagada",null,desde,hasta," Cliente ",pageable)).thenReturn(new PageImpl<>(List.of(venta()),pageable,10));
        String sort="{\"empty\":false,\"sorted\":true,\"unsorted\":false}";
        String pageJSON="{\"content\":["+ventaJSON("transferencia","pagada",null)+"],\"pageable\":{\"pageNumber\":2,\"pageSize\":3,\"sort\":"+sort+",\"offset\":6,\"paged\":true,\"unpaged\":false},\"last\":false,\"totalPages\":4,\"totalElements\":10,\"size\":3,\"number\":2,\"sort\":"+sort+",\"first\":false,\"numberOfElements\":1,\"empty\":false}";
        for(String permiso:List.of("venta.gestion.ver.propio","venta.gestion.ver.todos"))mvc.perform(get("/api/ventas/buscar").with(principal(permiso))
            .param("metodoPago","transferencia").param("salonId",SALON.toString()).param("estado","pagada")
            .param("desde","2026-08-01").param("hasta","2026-08-24").param("busqueda"," Cliente ")
            .param("page","2").param("size","3").param("sort","creadoEn,desc"))
            .andExpect(status().isOk()).andExpect(content().json(pageJSON,org.springframework.test.json.JsonCompareMode.STRICT));
        verify(ventas).historial("transferencia",SALON,"pagada",ACTOR,desde,hasta," Cliente ",pageable);
        verify(ventas).historial("transferencia",SALON,"pagada",null,desde,hasta," Cliente ",pageable);
        mvc.perform(get("/api/ventas").with(principal("venta.gestion.ver.propio"))).andExpect(status().isForbidden());verify(ventas,never()).historial();
        when(ventas.historial()).thenReturn(List.of(venta()));mvc.perform(get("/api/ventas").with(principal("venta.gestion.ver.todos")))
            .andExpect(status().isOk()).andExpect(content().json("["+ventaJSON("transferencia","pagada",null)+"]",org.springframework.test.json.JsonCompareMode.STRICT));
        verify(ventas).historial();
    }
    @Test void M10_catalogoPermisosCrearEditarYHabilitarConDeshabilitar() throws Exception {
        var actividades=List.of(new ActividadPaqueteRequest(actividad().getId(),2));
        var crear=new CrearPaqueteRequest("Dummy","Descripción dummy",100,30,"2 clases",true,7,actividades);
        var actualizar=new ActualizarPaqueteRequest("Dummy","Descripción dummy",100,30,"2 clases",true,true,7,actividades);
        String request="{\"nombre\":\"Dummy\",\"descripcion\":\"Descripción dummy\",\"precioCentavos\":100,\"vigenciaDias\":30,\"unitarioTexto\":\"2 clases\",\"destacado\":true,\"orden\":7,\"actividades\":[{\"tipoActividadId\":\""+actividad().getId()+"\",\"cantidadClases\":2}]}";
        when(gestion.listarTodos()).thenReturn(List.of(paqueteDTO()));when(gestion.crear(crear)).thenReturn(paqueteDTO());
        when(gestion.actualizar(PAQUETE,actualizar)).thenReturn(paqueteDTO());when(gestion.habilitar(PAQUETE)).thenReturn(paqueteDTO());when(gestion.deshabilitar(PAQUETE)).thenReturn(paqueteDTO());
        mvc.perform(get("/api/ventas/servicios").with(principal("venta.servicios.vista"))).andExpect(status().isOk()).andExpect(content().json("["+paqueteJSON()+"]",org.springframework.test.json.JsonCompareMode.STRICT));
        verify(gestion).listarTodos();
        mvc.perform(post("/api/ventas/servicios").with(principal("venta.servicios.gestionar.crear")).contentType("application/json").content(request))
            .andExpect(status().isCreated()).andExpect(content().json(paqueteJSON(),org.springframework.test.json.JsonCompareMode.STRICT));
        verify(gestion).crear(crear);
        mvc.perform(put("/api/ventas/servicios/{id}",PAQUETE).with(principal("venta.servicios.gestionar.editar")).contentType("application/json").content(request.replace("\"orden\":7","\"orden\":7,\"activo\":true")))
            .andExpect(status().isOk()).andExpect(content().json(paqueteJSON(),org.springframework.test.json.JsonCompareMode.STRICT));
        verify(gestion).actualizar(PAQUETE,actualizar);
        for(String accion:List.of("habilitar","deshabilitar"))mvc.perform(patch("/api/ventas/servicios/{id}/"+accion,PAQUETE).with(principal("venta.servicios.gestionar.deshabilitar")))
            .andExpect(status().isOk()).andExpect(content().json(paqueteJSON(),org.springframework.test.json.JsonCompareMode.STRICT));
        verify(gestion).habilitar(PAQUETE);verify(gestion).deshabilitar(PAQUETE);
        mvc.perform(patch("/api/ventas/servicios/{id}/habilitar",PAQUETE).with(principal("venta.servicios.gestionar.habilitar"))).andExpect(status().isForbidden());
    }
    @Test void M10_privadas401SinAutoridad403Valid400Advice() throws Exception {
        for(String path:List.of("/api/ventas","/api/ventas/sedes","/api/ventas/buscar","/api/ventas/servicios")){
            mvc.perform(get(path)).andExpect(status().isUnauthorized());mvc.perform(get(path).with(principal())).andExpect(status().isForbidden());}
        mvc.perform(post("/api/ventas").with(principal()).contentType("application/json").content(body())).andExpect(status().isForbidden());
        mvc.perform(post("/api/ventas/servicios").with(principal()).contentType("application/json").content(catalogBody())).andExpect(status().isForbidden());
        mvc.perform(put("/api/ventas/servicios/{id}",PAQUETE).with(principal()).contentType("application/json").content(catalogBody().replace("\"orden\":0","\"orden\":0,\"activo\":true"))).andExpect(status().isForbidden());
        mvc.perform(patch("/api/ventas/{id}/reembolsar",COMPRA).with(principal()).contentType("application/json").content("{\"motivo\":\"dummy\"}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/ventas").with(principal("venta.registrar.crear")).contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.clienteId").exists());
        mvc.perform(post("/api/ventas").with(principal("venta.registrar.crear")).contentType("application/json").content(body().replace("transferencia","stripe"))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/ventas/carrito").with(principal("venta.registrar.crear")).contentType("application/json").content("{\"items\":[]}" )).andExpect(status().isBadRequest());
        mvc.perform(patch("/api/ventas/{id}/reembolsar",COMPRA).with(principal("venta.gestion.gestionar")).contentType("application/json").content("{\"motivo\":\" \"}" )).andExpect(status().isBadRequest());
        mvc.perform(post("/api/ventas/servicios").with(principal("venta.servicios.gestionar.crear")).contentType("application/json").content("{}" )).andExpect(status().isBadRequest());
        verifyNoInteractions(ventas,gestion);
        when(ventas.registrarVenta(CLIENTE,PAQUETE,SALON,"transferencia",ACTOR)).thenThrow(new ValidacionException("Sede inválida"));
        mvc.perform(post("/api/ventas").with(principal("venta.registrar.crear")).contentType("application/json").content(body())).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Sede inválida"));
        when(gestion.habilitar(PAQUETE)).thenThrow(new ResourceNotFoundException("Paquete no encontrado"));
        mvc.perform(patch("/api/ventas/servicios/{id}/habilitar",PAQUETE).with(principal("venta.servicios.gestionar.deshabilitar"))).andExpect(status().isNotFound());
    }
    @Test void M10_carritoYDeshabilitar401403SinEfectos() throws Exception {
        mvc.perform(post("/api/ventas/carrito").contentType("application/json").content(carritoBody())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/ventas/carrito").with(principal()).contentType("application/json").content(carritoBody())).andExpect(status().isForbidden());
        mvc.perform(patch("/api/ventas/servicios/{id}/deshabilitar",PAQUETE)).andExpect(status().isUnauthorized());
        mvc.perform(patch("/api/ventas/servicios/{id}/deshabilitar",PAQUETE).with(principal("venta.servicios.gestionar.habilitar"))).andExpect(status().isForbidden());
        verifyNoInteractions(ventas,gestion);
    }
    @Test void M10_actualizarValid400CamposAisladosYNestedSinEfectos() throws Exception {
        String valid="{\"nombre\":\"Dummy\",\"descripcion\":\"Descripción dummy\",\"precioCentavos\":100,\"vigenciaDias\":30,\"unitarioTexto\":\"2 clases\",\"destacado\":true,\"activo\":true,\"orden\":7,\"actividades\":[{\"tipoActividadId\":\""+actividad().getId()+"\",\"cantidadClases\":2}]}";
        mvc.perform(put("/api/ventas/servicios/{id}",PAQUETE).with(principal("venta.servicios.gestionar.editar")).contentType("application/json").content(valid.replace("\"nombre\":\"Dummy\"","\"nombre\":\" \"")))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.nombre").exists()).andExpect(jsonPath("$.fieldErrors.length()").value(1));
        mvc.perform(put("/api/ventas/servicios/{id}",PAQUETE).with(principal("venta.servicios.gestionar.editar")).contentType("application/json").content(valid.replace("\"precioCentavos\":100","\"precioCentavos\":0")))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.precioCentavos").exists()).andExpect(jsonPath("$.fieldErrors.length()").value(1));
        mvc.perform(put("/api/ventas/servicios/{id}",PAQUETE).with(principal("venta.servicios.gestionar.editar")).contentType("application/json").content(valid.replace("\"cantidadClases\":2","\"cantidadClases\":0")))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors['actividades[0].cantidadClases']").exists()).andExpect(jsonPath("$.fieldErrors.length()").value(1));
        verify(gestion,never()).actualizar(any(),any());verifyNoInteractions(gestion,ventas);
    }
    @Test void M10_carritoValid400ItemAisladoSinEfectos() throws Exception {
        mvc.perform(post("/api/ventas/carrito").with(principal("venta.registrar.crear")).contentType("application/json").content(carritoBody().replace("\"cantidad\":1","\"cantidad\":0")))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors['items[0].cantidad']").exists()).andExpect(jsonPath("$.fieldErrors.length()").value(1));
        verifyNoInteractions(ventas,gestion);
    }
    @TestConfiguration(proxyBeanMethods=false) static class Config {
        @Bean VentaService ventaService(){return mock(VentaService.class);}
        @Bean PaqueteGestionService gestion(){return mock(PaqueteGestionService.class);}
        @Bean PaqueteRepository paquetes(){return mock(PaqueteRepository.class);}
        @Bean JwtAuthFilter jwtAuthFilter(){return new JwtAuthFilter(null,null);}
    }
}
