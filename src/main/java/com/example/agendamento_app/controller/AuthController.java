// controller/AuthController.java
package com.example.agendamento_app.controller;

import com.example.agendamento_app.model.Usuario;
import com.example.agendamento_app.repository.UsuarioRepository;
import com.example.agendamento_app.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String senha) {
        System.out.println("🔐 Tentativa de login: " + email);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, senha)
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("id", usuario.getId());
            response.put("nome", usuario.getNome());
            response.put("email", usuario.getEmail());
            response.put("papel", usuario.getPapel());
            response.put("empresaId", usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null);

            System.out.println("✅ Login realizado com sucesso: " + email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ Erro no login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).body(Map.of("error", "Credenciais inválidas: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-admin")
    public String resetAdmin() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Remover admin existente se houver
        usuarioRepository.findByEmail("admin@barbearia.com").ifPresent(u -> {
            usuarioRepository.delete(u);
            System.out.println("Admin antigo removido");
        });

        // Criar novo admin
        Usuario usuario = new Usuario();
        usuario.setEmail("admin@barbearia.com");
        usuario.setSenha(encoder.encode("123456"));
        usuario.setNome("Administrador");
        usuario.setPapel("DONO");
        usuario.setAtivo(true);

        usuarioRepository.save(usuario);

        // Verificar se salvou corretamente
        Usuario saved = usuarioRepository.findByEmail("admin@barbearia.com").get();

        return String.format(
                "✅ Admin criado com sucesso!\n\n" +
                        "Email: %s\n" +
                        "Senha: 123456\n" +
                        "Senha criptografada: %s\n" +
                        "BCrypt válido: %s",
                saved.getEmail(),
                saved.getSenha(),
                saved.getSenha().startsWith("$2a$")
        );
    }
}