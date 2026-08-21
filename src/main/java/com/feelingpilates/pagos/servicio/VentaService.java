package com.feelingpilates.pagos.servicio;

import com.feelingpilates.exception.ResourceNotFoundException;
import com.feelingpilates.exception.ValidacionException;
import com.feelingpilates.pagos.dto.ItemCarritoRequest;
import com.feelingpilates.pagos.dto.SedeVentaResponse;
import com.feelingpilates.pagos.dto.VentaCarritoResponse;
import com.feelingpilates.pagos.dto.VentaResponse;
import com.feelingpilates.pagos.entidad.Compra;
import com.feelingpilates.pagos.entidad.Compra.EstadoCompra;
import com.feelingpilates.pagos.entidad.Compra.MetodoPago;
import com.feelingpilates.pagos.entidad.Paquete;
import com.feelingpilates.pagos.repositorio.CompraRepository;
import com.feelingpilates.pagos.repositorio.PaqueteRepository;
import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.repositorio.SalonRepository;
import com.feelingpilates.usuarios.entidad.Rol;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.entidad.UsuarioRol;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Ventas presenciales cobradas en caja (efectivo/transferencia), sin pasar por Stripe. */
@Service
public class VentaService {

    private static final OffsetDateTime FECHA_MINIMA = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime FECHA_MAXIMA = OffsetDateTime.of(9999, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC);

    private final PaqueteRepository paqueteRepository;
    private final CompraRepository compraRepository;
    private final UsuarioRepository usuarioRepository;
    private final SalonRepository salonRepository;

    public VentaService(PaqueteRepository paqueteRepository, CompraRepository compraRepository,
                        UsuarioRepository usuarioRepository, SalonRepository salonRepository) {
        this.paqueteRepository = paqueteRepository;
        this.compraRepository = compraRepository;
        this.usuarioRepository = usuarioRepository;
        this.salonRepository = salonRepository;
    }

    /** Sedes en las que el usuario puede registrar una venta: todas si es ADMIN/SUPER_ADMIN, solo las suyas si es PERSONAL. */
    @Transactional(readOnly = true)
    public List<SedeVentaResponse> sedesDisponibles(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (esGlobal(usuario)) {
            return salonRepository.listarActivos().stream()
                    .map(s -> new SedeVentaResponse(s.getId(), s.getNombre()))
                    .toList();
        }
        return usuario.getRoles().stream()
                .filter(ur -> ur.getSalon() != null)
                .map(UsuarioRol::getSalon)
                .distinct()
                .map(s -> new SedeVentaResponse(s.getId(), s.getNombre()))
                .toList();
    }

    @Transactional
    public VentaResponse registrarVenta(UUID clienteId, UUID paqueteId, UUID salonId,
                                             String metodoPagoTexto, UUID registradaPorId) {
        return registrarVentaCarrito(clienteId, salonId, metodoPagoTexto,
                List.of(new ItemCarritoRequest(paqueteId, 1)), registradaPorId).items().get(0);
    }

    /**
     * Registra en una sola transaccion uno o varios paquetes (cada uno con su cantidad)
     * cobrados juntos en caja (un ticket): una fila Compra por unidad, compartiendo un
     * grupoCompraId y numeradas dentro del grupo. Si cualquier paquete es invalido no
     * se guarda nada.
     */
    @Transactional
    public VentaCarritoResponse registrarVentaCarrito(UUID clienteId, UUID salonId, String metodoPagoTexto,
                                                            List<ItemCarritoRequest> items, UUID registradaPorId) {
        MetodoPago metodoPago;
        try {
            metodoPago = MetodoPago.valueOf(metodoPagoTexto);
        } catch (IllegalArgumentException e) {
            throw new ValidacionException("Método de pago inválido");
        }
        if (metodoPago == MetodoPago.stripe) {
            throw new ValidacionException("Una venta de caja debe pagarse en efectivo o transferencia");
        }

        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        Usuario registradaPor = usuarioRepository.findById(registradaPorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Salon salon = validarSede(registradaPor, salonId);

        UUID grupoCompraId = UUID.randomUUID();
        int totalCentavos = 0;
        List<VentaResponse> respuestas = new java.util.ArrayList<>();
        int numeroItem = 1;
        for (ItemCarritoRequest item : items) {
            Paquete paquete = paqueteRepository.findById(item.paqueteId())
                    .filter(Paquete::isActivo)
                    .orElseThrow(() -> new ResourceNotFoundException("Paquete no encontrado"));

            for (int i = 0; i < item.cantidad(); i++) {
                Compra compra = new Compra();
                compra.setUsuario(cliente);
                compra.setPaquete(paquete);
                compra.setMontoCentavos(paquete.getPrecioCentavos());
                compra.setMetodoPago(metodoPago);
                compra.setRegistradaPor(registradaPor);
                compra.setSalon(salon);
                compra.setEstado(EstadoCompra.pagada);
                compra.setFechaExpiracion(OffsetDateTime.now().plusDays(paquete.getVigenciaDias()));
                compra.setGrupoCompraId(grupoCompraId);
                compra.setNumeroItem(numeroItem++);

                VentaResponse respuesta = aResponse(compraRepository.save(compra));
                respuestas.add(respuesta);
                totalCentavos += respuesta.montoCentavos();
            }
        }

        return new VentaCarritoResponse(grupoCompraId, respuestas, totalCentavos);
    }

    /** Marca como reembolsada una venta de caja (efectivo/transferencia) que aun este pagada. */
    @Transactional
    public VentaResponse reembolsarVenta(UUID compraId, String motivo) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada"));
        if (compra.getMetodoPago() == MetodoPago.stripe) {
            throw new ValidacionException("Las compras por Stripe se reembolsan desde Pagos, no desde Caja");
        }
        if (compra.getEstado() != EstadoCompra.pagada) {
            throw new ValidacionException("Solo se puede reembolsar una venta pagada");
        }
        compra.setEstado(EstadoCompra.reembolsada);
        compra.setMotivoEstado(motivo);
        return aResponse(compraRepository.save(compra));
    }

    private Salon validarSede(Usuario registradaPor, UUID salonId) {
        if (esGlobal(registradaPor)) {
            Salon salon = salonRepository.findById(salonId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));
            if (!salon.isActivo()) {
                throw new ValidacionException("La sede seleccionada no está activa");
            }
            return salon;
        }

        Set<UUID> sedesPropias = registradaPor.getRoles().stream()
                .filter(ur -> ur.getSalon() != null)
                .map(ur -> ur.getSalon().getId())
                .collect(Collectors.toSet());
        if (!sedesPropias.contains(salonId)) {
            throw new ValidacionException("No puedes registrar ventas en una sede que no te corresponde");
        }
        return registradaPor.getRoles().stream()
                .filter(ur -> ur.getSalon() != null && ur.getSalon().getId().equals(salonId))
                .map(UsuarioRol::getSalon)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));
    }

    private boolean esGlobal(Usuario usuario) {
        return usuario.getRoles().stream()
                .map(ur -> ur.getRol().getNombre())
                .anyMatch(nombre -> nombre.equals(Rol.ADMIN) || nombre.equals(Rol.SUPER_ADMIN));
    }

    @Transactional(readOnly = true)
    public List<VentaResponse> historial() {
        return compraRepository
                .findByMetodoPagoInOrderByCreadoEnDesc(List.of(MetodoPago.efectivo, MetodoPago.transferencia))
                .stream()
                .map(this::aResponse)
                .toList();
    }

    /**
     * @param registradaPorId si no es null, el historial se limita a las ventas registradas por ese usuario
     *                        (caso "ver solo lo propio"); si es null, se ve el historial completo.
     */
    @Transactional(readOnly = true)
    public Page<VentaResponse> historial(String metodoPagoTexto, UUID salonId, String estadoTexto,
                                              UUID registradaPorId, LocalDate desde, LocalDate hasta,
                                              String busqueda, Pageable pageable) {
        MetodoPago metodoPago = null;
        if (metodoPagoTexto != null && !metodoPagoTexto.isBlank()) {
            try {
                metodoPago = MetodoPago.valueOf(metodoPagoTexto);
            } catch (IllegalArgumentException e) {
                throw new ValidacionException("Método de pago inválido");
            }
        }
        EstadoCompra estado = null;
        if (estadoTexto != null && !estadoTexto.isBlank()) {
            try {
                estado = EstadoCompra.valueOf(estadoTexto);
            } catch (IllegalArgumentException e) {
                throw new ValidacionException("Estado inválido");
            }
        }
        // Siempre se envía un rango no nulo: Postgres no puede inferir el tipo de un
        // parámetro que solo aparece en "is null" sin otro contexto de tipo.
        OffsetDateTime desdeInicioDia = desde != null
                ? desde.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime()
                : FECHA_MINIMA;
        OffsetDateTime hastaFinDia = hasta != null
                ? hasta.plusDays(1).atStartOfDay(ZoneOffset.UTC).toOffsetDateTime().minusNanos(1)
                : FECHA_MAXIMA;
        String busquedaFiltro = (busqueda == null || busqueda.isBlank()) ? null : busqueda.trim();

        return compraRepository
                .buscarVentas(List.of(MetodoPago.efectivo, MetodoPago.transferencia), metodoPago, salonId,
                        estado, registradaPorId, desdeInicioDia, hastaFinDia, busquedaFiltro, pageable)
                .map(this::aResponse);
    }

    private VentaResponse aResponse(Compra compra) {
        return new VentaResponse(
                compra.getId(),
                compra.getUsuario().getNombre(),
                compra.getPaquete().getNombre(),
                compra.getMontoCentavos(),
                compra.getMetodoPago().name(),
                compra.getEstado().name(),
                compra.getCreadoEn(),
                compra.getFechaExpiracion(),
                compra.getRegistradaPor() != null ? compra.getRegistradaPor().getNombre() : null,
                compra.getSalon() != null ? compra.getSalon().getNombre() : null,
                compra.getGrupoCompraId(),
                compra.getNumeroItem(),
                compra.getMotivoEstado());
    }
}
