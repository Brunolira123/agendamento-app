// controller/ServicoController.java
package com.example.agendamento_app.controller;

import com.example.agendamento_app.model.Empresa;
import com.example.agendamento_app.model.Servico;
import com.example.agendamento_app.model.Usuario;
import com.example.agendamento_app.repository.EmpresaRepository;
import com.example.agendamento_app.repository.ServicoRepository;
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
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ServicoController {

    private final ServicoRepository servicoRepository;
    private final EmpresaRepository empresaRepository;

    // Listar todos os serviços da empresa
    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) Boolean ativos,
                                    HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            List<Servico> servicos;
            if (ativos != null && ativos) {
                servicos = servicoRepository.findByEmpresaIdAndAtivoTrue(usuario.getEmpresa().getId());
            } else {
                servicos = servicoRepository.findByEmpresaId(usuario.getEmpresa().getId());
            }

            return ResponseEntity.ok(servicos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Buscar serviço por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Servico servico = servicoRepository.findById(id)
                    .orElse(null);

            if (servico == null || !servico.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
                return ResponseEntity.status(404).body(Map.of("error", "Serviço não encontrado"));
            }

            return ResponseEntity.ok(servico);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Criar novo serviço
    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody Servico servico,
                                   HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
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

    // Atualizar serviço
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id,
                                       @Valid @RequestBody Servico servicoAtualizado,
                                       HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Servico servico = servicoRepository.findById(id)
                    .orElse(null);

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

    // Ativar/Desativar serviço
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Servico servico = servicoRepository.findById(id)
                    .orElse(null);

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

    // Deletar serviço
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Servico servico = servicoRepository.findById(id)
                    .orElse(null);

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