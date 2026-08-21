package com.feelingpilates.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Autentica cada request con su JWT, pero NO confia en los claims "roles"/
 * "permisos" que trae el token: los recalcula desde la base de datos en cada
 * request via PermisoResolver. Asi, revocar o cambiar permisos de un rol
 * aplica de inmediato a las sesiones ya activas, en vez de esperar a que el
 * usuario vuelva a iniciar sesion o el token expire.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ContextoAutenticacionService contextoAutenticacionService;

    public JwtAuthFilter(JwtService jwtService, ContextoAutenticacionService contextoAutenticacionService) {
        this.jwtService = jwtService;
        this.contextoAutenticacionService = contextoAutenticacionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.validarYObtenerClaims(header.substring(7));
                UUID usuarioId = UUID.fromString(claims.getSubject());

                contextoAutenticacionService.resolver(usuarioId)
                        .filter(ContextoAutenticacionService.Contexto::activo)
                        .ifPresent(contexto -> {
                            List<GrantedAuthority> authorities = new ArrayList<>();
                            contexto.roles().forEach(rol -> authorities.add(new SimpleGrantedAuthority("ROLE_" + rol)));
                            contexto.permisos().forEach(permiso -> authorities.add(new SimpleGrantedAuthority(permiso)));

                            UsuarioAutenticado principal = new UsuarioAutenticado(contexto.id(), contexto.correo());
                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        });
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
