package com.example.agendamento_app.controller;

import com.example.agendamento_app.model.Assinatura;
import com.example.agendamento_app.model.Empresa;
import com.example.agendamento_app.model.Plano;
import com.example.agendamento_app.model.Usuario;
import com.example.agendamento_app.repository.AssinaturaRepository;
import com.example.agendamento_app.repository.EmpresaRepository;
import com.example.agendamento_app.repository.PlanoRepository;
import com.example.agendamento_app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assinaturas")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
public class AssinaturaController {

    private final AssinaturaRepository assinaturaRepository;
    private final EmpresaRepository empresaRepository;
    private final PlanoRepository planoRepository;
    private final UsuarioRepository usuarioRepository;

    private Usuario getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    // Criar nova assinatura
    @PostMapping
    public ResponseEntity<?> criarAssinatura(@RequestBody Map<String, Object> request) {
        try {
            Usuario usuario = getUsuarioLogado();
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Long empresaId = usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null;
            if (empresaId == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Usuário não associado a uma empresa"));
            }

            Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
            if (empresa == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Empresa não encontrada"));
            }

            Long planoId = request.get("planoId") != null ?
                    Long.valueOf(request.get("planoId").toString()) : 2L;
            String periodo = request.get("periodo") != null ?
                    request.get("periodo").toString() : "mensal";

            Plano plano = planoRepository.findById(planoId).orElse(null);
            if (plano == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Plano não encontrado"));
            }

            // Verificar se já existe assinatura ativa
            Assinatura assinaturaExistente = assinaturaRepository
                    .findByEmpresaIdAndStatus(empresaId, "ATIVA")
                    .orElse(null);

            if (assinaturaExistente != null) {
                return ResponseEntity.status(400).body(Map.of("error", "Já existe uma assinatura ativa para esta empresa"));
            }

            BigDecimal valor = periodo.equals("anual") ? plano.getPrecoAnual() : plano.getPrecoMensal();

            Assinatura assinatura = new Assinatura();
            assinatura.setEmpresa(empresa);
            assinatura.setPlano(plano);
            assinatura.setStatus("TESTE");
            assinatura.setPeriodo(periodo);
            assinatura.setValor(valor);
            assinatura.setDataInicio(LocalDateTime.now());
            assinatura.setDataFim(LocalDateTime.now().plusDays(7));
            assinatura.setDiasTeste(7);

            Assinatura salva = assinaturaRepository.save(assinatura);

            Map<String, Object> response = new HashMap<>();
            response.put("id", salva.getId());
            response.put("status", salva.getStatus());
            response.put("periodo", salva.getPeriodo());
            response.put("valor", salva.getValor());
            response.put("dataFim", salva.getDataFim());
            response.put("diasTeste", salva.getDiasTeste());
            response.put("requiresPayment", false);

            Map<String, Object> planoMap = new HashMap<>();
            planoMap.put("id", plano.getId());
            planoMap.put("nome", plano.getNome());
            planoMap.put("slug", plano.getSlug());
            response.put("plano", planoMap);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("❌ Erro ao criar assinatura: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Buscar assinatura atual da empresa
    @GetMapping("/atual")
    public ResponseEntity<?> buscarAssinaturaAtual() {
        try {
            Usuario usuario = getUsuarioLogado();
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Long empresaId = usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null;
            if (empresaId == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Usuário não associado a uma empresa"));
            }

            // Buscar assinatura ativa ou em teste
            Assinatura assinatura = assinaturaRepository
                    .findByEmpresaIdAndStatus(empresaId, "ATIVA")
                    .orElse(assinaturaRepository.findByEmpresaIdAndStatus(empresaId, "TESTE").orElse(null));

            if (assinatura == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Nenhuma assinatura encontrada"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", assinatura.getId());
            response.put("status", assinatura.getStatus());
            response.put("periodo", assinatura.getPeriodo());
            response.put("valor", assinatura.getValor());
            response.put("dataInicio", assinatura.getDataInicio());
            response.put("dataFim", assinatura.getDataFim());
            response.put("diasTeste", assinatura.getDiasTeste());

            Map<String, Object> planoMap = new HashMap<>();
            planoMap.put("id", assinatura.getPlano().getId());
            planoMap.put("nome", assinatura.getPlano().getNome());
            planoMap.put("slug", assinatura.getPlano().getSlug());
            planoMap.put("precoMensal", assinatura.getPlano().getPrecoMensal());
            response.put("plano", planoMap);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Erro ao buscar assinatura: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Cancelar assinatura
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelarAssinatura(@PathVariable Long id) {
        try {
            Usuario usuario = getUsuarioLogado();
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Assinatura assinatura = assinaturaRepository.findById(id).orElse(null);
            if (assinatura == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Assinatura não encontrada"));
            }

            assinatura.setStatus("CANCELADA");
            assinatura.setDataCancelamento(LocalDateTime.now());
            assinaturaRepository.save(assinatura);

            return ResponseEntity.ok(Map.of("message", "Assinatura cancelada com sucesso"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Listar histórico de assinaturas
    @GetMapping("/historico")
    public ResponseEntity<?> listarHistorico() {
        try {
            Usuario usuario = getUsuarioLogado();
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Long empresaId = usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null;
            if (empresaId == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Usuário não associado a uma empresa"));
            }

            List<Assinatura> assinaturas = assinaturaRepository.findByEmpresaIdOrderByCreatedAtDesc(empresaId);
            return ResponseEntity.ok(assinaturas);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}