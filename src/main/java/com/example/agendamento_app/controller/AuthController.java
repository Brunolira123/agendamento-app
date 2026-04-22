// controller/AuthController.java
package com.example.agendamento_app.controller;

import com.example.agendamento_app.model.Usuario;
import com.example.agendamento_app.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController  // ← Mudar para @RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email,
                                   @RequestParam String senha,
                                   HttpSession session) {
        System.out.println("Tentando login - Sessão atual: " + session.getId());

        if (authService.login(email, senha, session)) {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            System.out.println("Login realizado com sucesso para: " + email);
            System.out.println("Sessão após login: " + session.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("id", usuario.getId());
            response.put("nome", usuario.getNome());
            response.put("email", usuario.getEmail());
            response.put("papel", usuario.getPapel());
            response.put("empresaId", usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null);

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciais inválidas"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logout realizado com sucesso"));
    }

    @GetMapping("/session")
    public ResponseEntity<?> checkSession(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario != null) {
            return ResponseEntity.ok(Map.of(
                    "loggedIn", true,
                    "usuario", usuario.getEmail(),
                    "sessionId", session.getId()
            ));
        }
        return ResponseEntity.ok(Map.of("loggedIn", false));
    }
}