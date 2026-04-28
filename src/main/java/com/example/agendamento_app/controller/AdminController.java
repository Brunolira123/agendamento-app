package com.example.agendamento_app.controller;

import com.example.agendamento_app.model.*;
import com.example.agendamento_app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlanoRepository planoRepository;
    private final AssinaturaRepository assinaturaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ProfissionalRepository profissionalRepository;

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        // Total de empresas
        long totalEmpresas = empresaRepository.count();
        dashboard.put("totalEmpresas", totalEmpresas);

        // Total de usuários (donos)
        long totalUsuarios = usuarioRepository.count();
        dashboard.put("totalUsuarios", totalUsuarios);

        // Novas empresas últimos 30 dias
        LocalDateTime trintaDiasAtras = LocalDateTime.now().minusDays(30);
        long novasEmpresas = empresaRepository.countByCreatedAtAfter(trintaDiasAtras);
        dashboard.put("novasEmpresas", novasEmpresas);

        // Assinaturas ativas
        long assinaturasAtivas = assinaturaRepository.countByStatus("ATIVA");
        long assinaturasTeste = assinaturaRepository.countByStatus("TESTE");
        dashboard.put("assinaturasAtivas", assinaturasAtivas);
        dashboard.put("assinaturasTeste", assinaturasTeste);

        // Receita mensal
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate fimMes = inicioMes.plusMonths(1);
        BigDecimal receitaMensal = pagamentoRepository.sumValorByDataPagamentoBetween(inicioMes.atStartOfDay(), fimMes.atStartOfDay());
        dashboard.put("receitaMensal", receitaMensal != null ? receitaMensal : BigDecimal.ZERO);

        // Receita anual
        LocalDate inicioAno = LocalDate.now().withDayOfYear(1);
        LocalDate fimAno = inicioAno.plusYears(1);
        BigDecimal receitaAnual = pagamentoRepository.sumValorByDataPagamentoBetween(inicioAno.atStartOfDay(), fimAno.atStartOfDay());
        dashboard.put("receitaAnual", receitaAnual != null ? receitaAnual : BigDecimal.ZERO);

        // Planos mais vendidos
        List<Object[]> planosVendidos = assinaturaRepository.countByPlanoGroup();
        dashboard.put("planosVendidos", planosVendidos);

        // Próximos vencimentos (7 dias)
        LocalDateTime hoje = LocalDateTime.now();
        LocalDateTime daqui7Dias = hoje.plusDays(7);
        List<Assinatura> vencendoEmBreve = assinaturaRepository.findByDataFimBetweenAndStatus(hoje, daqui7Dias, "ATIVA");
        dashboard.put("vencendoEmBreve", vencendoEmBreve.size());

        return ResponseEntity.ok(dashboard);
    }

    // ==================== CRUD PLANOS ====================

    @GetMapping("/planos")
    public ResponseEntity<List<Plano>> listarPlanos() {
        return ResponseEntity.ok(planoRepository.findAll());
    }

    @GetMapping("/planos/{id}")
    public ResponseEntity<Plano> buscarPlano(@PathVariable Long id) {
        return planoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/planos")
    public ResponseEntity<Plano> criarPlano(@RequestBody Plano plano) {
        plano.setCreatedAt(LocalDateTime.now());
        plano.setUpdatedAt(LocalDateTime.now());
        Plano salvo = planoRepository.save(plano);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/planos/{id}")
    public ResponseEntity<Plano> atualizarPlano(@PathVariable Long id, @RequestBody Plano plano) {
        if (!planoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        plano.setId(id);
        plano.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(planoRepository.save(plano));
    }

    @DeleteMapping("/planos/{id}")
    public ResponseEntity<Void> deletarPlano(@PathVariable Long id) {
        if (!planoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        planoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== EMPRESAS ====================

    @GetMapping("/empresas")
    public ResponseEntity<List<Empresa>> listarEmpresas(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<Empresa> empresas;
        if (search != null && !search.isEmpty()) {
            empresas = empresaRepository.findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search);
        } else {
            empresas = empresaRepository.findAll();
        }

        // Paginação simples
        int start = page * size;
        int end = Math.min(start + size, empresas.size());
        List<Empresa> paginado = empresas.subList(start, end);

        return ResponseEntity.ok(paginado);
    }

    @GetMapping("/empresas/{id}")
    public ResponseEntity<Map<String, Object>> detalhesEmpresa(@PathVariable Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

        Map<String, Object> detalhes = new HashMap<>();
        detalhes.put("empresa", empresa);

        // Dono da empresa
        Usuario dono = usuarioRepository.findByEmpresaIdAndPapel(id, "DONO");
        detalhes.put("dono", dono);

        // Assinatura atual
        Assinatura assinaturaAtual = assinaturaRepository.findByEmpresaIdAndStatus(id, "ATIVA")
                .orElse(null);
        detalhes.put("assinaturaAtual", assinaturaAtual);

        // Histórico de assinaturas
        List<Assinatura> historicoAssinaturas = assinaturaRepository.findByEmpresaIdOrderByCreatedAtDesc(id);
        detalhes.put("historicoAssinaturas", historicoAssinaturas);

        // Pagamentos
        List<Pagamento> pagamentos = pagamentoRepository.findByAssinaturaEmpresaIdOrderByCreatedAtDesc(id);
        detalhes.put("pagamentos", pagamentos);

        // Estatísticas
        long totalAgendamentos = agendamentoRepository.countByEmpresaId(id);
        long totalProfissionais = profissionalRepository.countByEmpresaId(id);
        detalhes.put("totalAgendamentos", totalAgendamentos);
        detalhes.put("totalProfissionais", totalProfissionais);

        return ResponseEntity.ok(detalhes);
    }

    // ==================== FINANCEIRO ====================

    @GetMapping("/financeiro/assinaturas")
    public ResponseEntity<Map<String, Object>> listarAssinaturas(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<Assinatura> assinaturas;
        if (status != null && !status.isEmpty()) {
            assinaturas = assinaturaRepository.findByStatus(status);
        } else {
            assinaturas = assinaturaRepository.findAll();
        }

        // Paginação
        int start = page * size;
        int end = Math.min(start + size, assinaturas.size());

        Map<String, Object> response = new HashMap<>();
        response.put("total", assinaturas.size());
        response.put("assinaturas", assinaturas.subList(start, end));

        // Resumo financeiro
        BigDecimal receitaMensal = pagamentoRepository.sumValorByDataPagamentoBetween(
                LocalDate.now().withDayOfMonth(1).atStartOfDay(),
                LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay()
        );
        response.put("receitaMensal", receitaMensal != null ? receitaMensal : BigDecimal.ZERO);

        long assinaturasAtivas = assinaturaRepository.countByStatus("ATIVA");
        long assinaturasCanceladas = assinaturaRepository.countByStatus("CANCELADA");
        response.put("assinaturasAtivas", assinaturasAtivas);
        response.put("assinaturasCanceladas", assinaturasCanceladas);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/financeiro/pagamentos")
    public ResponseEntity<List<Pagamento>> listarPagamentos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        if (inicio != null && fim != null) {
            return ResponseEntity.ok(pagamentoRepository.findByDataPagamentoBetween(inicio.atStartOfDay(), fim.atStartOfDay()));
        }
        return ResponseEntity.ok(pagamentoRepository.findAllByOrderByCreatedAtDesc());
    }
}