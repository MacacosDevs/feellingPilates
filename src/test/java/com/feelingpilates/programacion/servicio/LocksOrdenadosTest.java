package com.feelingpilates.programacion.servicio;

import com.feelingpilates.ubicaciones.entidad.Salon;
import com.feelingpilates.ubicaciones.servicio.SalonLock;
import com.feelingpilates.ubicaciones.servicio.SalonLocks;
import com.feelingpilates.usuarios.entidad.Usuario;
import com.feelingpilates.usuarios.repositorio.UsuarioRepository;
import com.feelingpilates.usuarios.servicio.InstructorLocks;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocksOrdenadosTest {

    private static final UUID MENOR = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID MAYOR = UUID.fromString("77777777-7777-7777-7777-777777777777");

    @Test
    void salonesSeDeduplicanYBloqueanEnOrdenUuidAscendente() {
        SalonLock lock = mock(SalonLock.class);
        Salon menor = salon(MENOR);
        Salon mayor = salon(MAYOR);
        when(lock.adquirir(MENOR)).thenReturn(menor);
        when(lock.adquirir(MAYOR)).thenReturn(mayor);

        List<Salon> resultado = new SalonLocks(lock)
                .adquirirOrdenados(List.of(MAYOR, MENOR, MAYOR));

        assertThat(resultado).containsExactly(menor, mayor);
        InOrder orden = inOrder(lock);
        orden.verify(lock).adquirir(MENOR);
        orden.verify(lock).adquirir(MAYOR);
        verify(lock, times(2)).adquirir(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void instructoresSeDeduplicanYBloqueanEnOrdenUuidAscendente() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        Usuario menor = usuario(MENOR);
        Usuario mayor = usuario(MAYOR);
        when(repository.bloquearParaActualizar(MENOR)).thenReturn(Optional.of(menor));
        when(repository.bloquearParaActualizar(MAYOR)).thenReturn(Optional.of(mayor));

        List<Usuario> resultado = new InstructorLocks(repository)
                .adquirirOrdenados(List.of(MAYOR, MENOR, MAYOR));

        assertThat(resultado).containsExactly(menor, mayor);
        InOrder orden = inOrder(repository);
        orden.verify(repository).bloquearParaActualizar(MENOR);
        orden.verify(repository).bloquearParaActualizar(MAYOR);
        verify(repository, times(2)).bloquearParaActualizar(org.mockito.ArgumentMatchers.any());
    }

    private Salon salon(UUID id) {
        Salon salon = new Salon();
        salon.setId(id);
        return salon;
    }

    private Usuario usuario(UUID id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        return usuario;
    }
}
