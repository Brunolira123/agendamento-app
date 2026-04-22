// UsuarioController.java
package com.example.agendamento_app.controller;

import com.example.agendamento_app.DTO.ErrorResponseDTO;
import com.example.agendamento_app.DTO.UsuarioLoginResponseDTO;
import com.example.agendamento_app.model.Usuario;
import com.example.agendamento_app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;


    @GetMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String senha) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailAndSenha(email, senha);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            UsuarioLoginResponseDTO dto = new UsuarioLoginResponseDTO();
            dto.setId(usuario.getId());
            dto.setNome(usuario.getNome());
            dto.setEmail(usuario.getEmail());
            dto.setEmpresaId(usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null);
            dto.setPapel(usuario.getPapel());
            dto.setAtivo(usuario.getAtivo());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.status(401).body(new ErrorResponseDTO("Credenciais inválidas"));
        }
    }
}