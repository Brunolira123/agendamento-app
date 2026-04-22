// controller/ProfissionalController.java
package com.example.agendamento_app.controller;

import com.example.agendamento_app.model.Empresa;
import com.example.agendamento_app.model.Profissional;
import com.example.agendamento_app.model.Usuario;
import com.example.agendamento_app.repository.EmpresaRepository;
import com.example.agendamento_app.repository.ProfissionalRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // Listar todos os profissionais da empresa
    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) Boolean ativos,
                                    HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            List<Profissional> profissionais;
            if (ativos != null && ativos) {
                profissionais = profissionalRepository.findByEmpresaIdAndAtivoTrue(usuario.getEmpresa().getId());
            } else {
                profissionais = profissionalRepository.findByEmpresaId(usuario.getEmpresa().getId());
            }

            return ResponseEntity.ok(profissionais);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Buscar profissional por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Profissional profissional = profissionalRepository.findById(id)
                    .orElse(null);

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
    public ResponseEntity<?> criar(@Valid @RequestBody Profissional profissional,
                                   HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
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
    public ResponseEntity<?> atualizar(@PathVariable Long id,
                                       @Valid @RequestBody Profissional profissionalAtualizado,
                                       HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Profissional profissional = profissionalRepository.findById(id)
                    .orElse(null);

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
    public ResponseEntity<?> toggleStatus(@PathVariable Long id, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Profissional profissional = profissionalRepository.findById(id)
                    .orElse(null);

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
    public ResponseEntity<?> deletar(@PathVariable Long id, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Profissional profissional = profissionalRepository.findById(id)
                    .orElse(null);

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