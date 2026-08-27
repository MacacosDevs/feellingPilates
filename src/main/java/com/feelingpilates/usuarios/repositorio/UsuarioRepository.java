package com.feelingpilates.usuarios.repositorio;

import com.feelingpilates.usuarios.entidad.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :usuarioId")
    Optional<Usuario> bloquearParaActualizar(@Param("usuarioId") UUID usuarioId);

    @EntityGraph(attributePaths = {"roles", "roles.rol", "roles.rol.permisos"})
    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    @Query("""
            select distinct u from Usuario u
            left join u.roles ur
            left join ur.rol r
            where (:rol is null or r.nombre = :rol)
            and (:busqueda is null
                 or lower(u.nombre) like lower(concat('%', cast(:busqueda as string), '%'))
                 or lower(u.correo) like lower(concat('%', cast(:busqueda as string), '%')))
            """)
    Page<Usuario> buscar(@Param("rol") String rol, @Param("busqueda") String busqueda, Pageable pageable);

    @Query("""
            select r.nombre as rol, count(distinct u.id) as total
            from Usuario u
            join u.roles ur
            join ur.rol r
            where u.estatus <> 'eliminado'
            group by r.nombre
            """)
    List<RolConteoProjection> contarPorRol();

    interface RolConteoProjection {
        String getRol();
        Long getTotal();
    }
}
