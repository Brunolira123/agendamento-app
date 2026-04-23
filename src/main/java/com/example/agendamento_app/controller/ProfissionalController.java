// controller/ProfissionalController.java - VERSÃO CORRIGIDA
package com.example.agendamento_app.controller;

import com.example.agendamento_app.model.Empresa;
import com.example.agendamento_app.model.Profissional;
import com.example.agendamento_app.model.Usuario;
import com.example.agendamento_app.repository.EmpresaRepository;
import com.example.agendamento_app.repository.ProfissionalRepository;
import com.example.agendamento_app.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profissionais")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ProfissionalController {

    private final ProfissionalRepository profissionalRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    // Método para obter usuário do SecurityContext (JWT)
    private Usuario getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("❌ Autenticação não encontrada no SecurityContext");
            return null;
        }

        String email = authentication.getName();
        System.out.println("✅ Email do token: " + email);
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    // Listar todos os profissionais da empresa
    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) Boolean ativos) {
        try {
            System.out.println("=== GET /api/profissionais ===");
            Usuario usuario = getUsuarioLogado();

            if (usuario == null) {
                System.out.println("❌ Usuário não encontrado no SecurityContext");
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            System.out.println("👤 Usuário: " + usuario.getEmail());
            System.out.println("🏢 Empresa ID: " + (usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : "null"));

            if (usuario.getEmpresa() == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Usuário não associado a uma empresa"));
            }

            List<Profissional> profissionais;
            if (ativos != null && ativos) {
                profissionais = profissionalRepository.findByEmpresaIdAndAtivoTrue(usuario.getEmpresa().getId());
            } else {
                profissionais = profissionalRepository.findByEmpresaId(usuario.getEmpresa().getId());
            }

            System.out.println("✅ Encontrados " + profissionais.size() + " profissionais");
            return ResponseEntity.ok(profissionais);
        } catch (Exception e) {
            System.err.println("❌ Erro: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Buscar profissional por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            Usuario usuario = getUsuarioLogado();
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Profissional profissional = profissionalRepository.findById(id).orElse(null);

            if (profissional == null || !profissional.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
                return ResponseEntity.status(404).body(Map.of("error", "Profissional não encontrado"));
            }

            return ResponseEntity.ok(profissional);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Criar novo profissional
    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody Profissional profissional) {
        try {
            Usuario usuario = getUsuarioLogado();
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Empresa empresa = empresaRepository.findById(usuario.getEmpresa().getId())
                    .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

            profissional.setEmpresa(empresa);
            profissional.setCreatedAt(LocalDateTime.now());
            profissional.setAtivo(true);

            Profissional salvo = profissionalRepository.save(profissional);
            return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Atualizar profissional
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody Profissional profissionalAtualizado) {
        try {
            Usuario usuario = getUsuarioLogado();
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Profissional profissional = profissionalRepository.findById(id).orElse(null);

            if (profissional == null || !profissional.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
                return ResponseEntity.status(404).body(Map.of("error", "Profissional não encontrado"));
            }

            profissional.setNome(profissionalAtualizado.getNome());
            profissional.setEmail(profissionalAtualizado.getEmail());
            profissional.setTelefone(profissionalAtualizado.getTelefone());

            Profissional salvo = profissionalRepository.save(profissional);
            return ResponseEntity.ok(salvo);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Ativar/Desativar profissional
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        try {
            Usuario usuario = getUsuarioLogado();
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Profissional profissional = profissionalRepository.findById(id).orElse(null);

            if (profissional == null || !profissional.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
                return ResponseEntity.status(404).body(Map.of("error", "Profissional não encontrado"));
            }

            profissional.setAtivo(!profissional.getAtivo());
            profissional = profissionalRepository.save(profissional);

            Map<String, Object> response = new HashMap<>();
            response.put("id", profissional.getId());
            response.put("ativo", profissional.getAtivo());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Deletar profissional
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            Usuario usuario = getUsuarioLogado();
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Profissional profissional = profissionalRepository.findById(id).orElse(null);

            if (profissional == null || !profissional.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
                return ResponseEntity.status(404).body(Map.of("error", "Profissional não encontrado"));
            }

            profissionalRepository.delete(profissional);
            return ResponseEntity.ok(Map.of("message", "Profissional removido com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}