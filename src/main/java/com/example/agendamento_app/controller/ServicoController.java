// controller/ServicoController.java
package com.example.agendamento_app.controller;

import com.example.agendamento_app.model.Empresa;
import com.example.agendamento_app.model.Servico;
import com.example.agendamento_app.model.Usuario;
import com.example.agendamento_app.repository.EmpresaRepository;
import com.example.agendamento_app.repository.ServicoRepository;
import com.example.agendamento_app.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
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
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ServicoController {

    private final ServicoRepository servicoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    private Usuario getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) Boolean ativos) {
        try {
            Usuario usuario = getUsuarioLogado();
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            if (usuario.getEmpresa() == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Usuário não associado a uma empresa"));
            }

            List<Servico> servicos;
            if (ativos != null && ativos) {
                servicos = servicoRepository.findByEmpresaIdAndAtivoTrue(usuario.getEmpresa().getId());
            } else {
                servicos = servicoRepository.findByEmpresaId(usuario.getEmpresa().getId());
            }

            return ResponseEntity.ok(servicos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            Usuario usuario = getUsuarioLogado();
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Servico servico = servicoRepository.findById(id).orElse(null);

            if (servico == null || !servico.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
                return ResponseEntity.status(404).body(Map.of("error", "Serviço não encontrado"));
            }

            return ResponseEntity.ok(servico);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody Servico servico) {
        try {
            Usuario usuario = getUsuarioLogado();
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Empresa empresa = empresaRepository.findById(usuario.getEmpresa().getId())
                    .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

            servico.setEmpresa(empresa);
            servico.setCreatedAt(LocalDateTime.now());
            servico.setAtivo(true);

            Servico salvo = servicoRepository.save(servico);
            return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody Servico servicoAtualizado) {
        try {
            Usuario usuario = getUsuarioLogado();
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Servico servico = servicoRepository.findById(id).orElse(null);

            if (servico == null || !servico.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
                return ResponseEntity.status(404).body(Map.of("error", "Serviço não encontrado"));
            }

            servico.setNome(servicoAtualizado.getNome());
            servico.setDescricao(servicoAtualizado.getDescricao());
            servico.setPreco(servicoAtualizado.getPreco());
            servico.setDuracaoMinutos(servicoAtualizado.getDuracaoMinutos());

            Servico salvo = servicoRepository.save(servico);
            return ResponseEntity.ok(salvo);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        try {
            Usuario usuario = getUsuarioLogado();
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Servico servico = servicoRepository.findById(id).orElse(null);

            if (servico == null || !servico.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
                return ResponseEntity.status(404).body(Map.of("error", "Serviço não encontrado"));
            }

            servico.setAtivo(!servico.getAtivo());
            servico = servicoRepository.save(servico);

            Map<String, Object> response = new HashMap<>();
            response.put("id", servico.getId());
            response.put("ativo", servico.getAtivo());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            Usuario usuario = getUsuarioLogado();
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Servico servico = servicoRepository.findById(id).orElse(null);

            if (servico == null || !servico.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
                return ResponseEntity.status(404).body(Map.of("error", "Serviço não encontrado"));
            }

            servicoRepository.delete(servico);
            return ResponseEntity.ok(Map.of("message", "Serviço removido com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}