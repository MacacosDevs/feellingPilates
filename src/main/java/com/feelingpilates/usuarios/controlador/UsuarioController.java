package com.feelingpilates.usuarios.controlador;

import com.feelingpilates.seguridad.UsuarioAutenticado;
import com.feelingpilates.usuarios.dto.ActualizarPerfilRequest;
import com.feelingpilates.usuarios.dto.FotoUsuario;
import com.feelingpilates.usuarios.dto.UsuarioResponse;
import com.feelingpilates.usuarios.servicio.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public UsuarioResponse miPerfil(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuarioService.obtenerPorId(usuario.id());
    }

    @PutMapping("/me")
    public UsuarioResponse actualizarMiPerfil(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                              @Valid @RequestBody ActualizarPerfilRequest request) {
        return usuarioService.actualizarPerfil(usuario.id(), request);
    }

    @PostMapping("/me/foto")
    public UsuarioResponse actualizarMiFoto(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                            @RequestParam("archivo") MultipartFile archivo) {
        return usuarioService.actualizarFoto(usuario.id(), archivo);
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> foto(@PathVariable UUID id) {
        FotoUsuario foto = usuarioService.obtenerFoto(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, foto.tipo())
                .body(foto.datos());
    }
}
