// controller/AgendamentoController.java
package com.example.agendamento_app.controller;

import com.example.agendamento_app.model.Agendamento;
import com.example.agendamento_app.model.enums.StatusAgendamento;
import com.example.agendamento_app.service.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agendamentos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    @PostMapping
    public ResponseEntity<Agendamento> criar(@Valid @RequestBody Agendamento agendamento) {
        Agendamento novo = agendamentoService.criarAgendamento(agendamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<?> atualizarStatus(
            @PathVariable Long id,
            @RequestParam StatusAgendamento status) {
        try {
            Agendamento atualizado = agendamentoService.atualizarStatus(id, status);
            return ResponseEntity.ok(atualizado);
        } catch (Exception e) {
            Map<String, String> erro = Map.of("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
        }
    }

    // ==================== NOVOS ENDPOINTS ====================

    // GET /api/agendamentos/empresa/{empresaId}/periodo
    @GetMapping("/empresa/{empresaId}/periodo")
    public ResponseEntity<List<Agendamento>> listarPorPeriodo(
            @PathVariable Long empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        List<Agendamento> agendamentos = agendamentoService.listarAgendamentosPorPeriodo(empresaId, inicio, fim);
        return ResponseEntity.ok(agendamentos);
    }

    // GET /api/agendamentos/empresa/{empresaId}/estatisticas
    @GetMapping("/empresa/{empresaId}/estatisticas")
    public ResponseEntity<Map<String, Object>> getEstatisticas(
            @PathVariable Long empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        Map<String, Object> estatisticas = agendamentoService.getEstatisticas(empresaId, data);
        return ResponseEntity.ok(estatisticas);
    }

    // GET /api/agendamentos/empresa/{empresaId}/hoje
    @GetMapping("/empresa/{empresaId}/hoje")
    public ResponseEntity<List<Agendamento>> agendamentosHoje(@PathVariable Long empresaId) {
        return ResponseEntity.ok(agendamentoService.listarAgendamentosDoDia(empresaId));
    }

    // GET /api/agendamentos/empresa/{empresaId}
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<Agendamento>> listarTodos(@PathVariable Long empresaId) {
        return ResponseEntity.ok(agendamentoService.listarTodosPorEmpresa(empresaId));
    }

    // GET /api/agendamentos?data=YYYY-MM-DD
    @GetMapping
    public ResponseEntity<List<Agendamento>> listarPorData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.plusDays(1).atStartOfDay();
        List<Agendamento> agendamentos = agendamentoService.listarPorData(inicio, fim);
        return ResponseEntity.ok(agendamentos);
    }

    // GET /api/agendamentos/estatisticas?data=YYYY-MM-DD
    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Object>> getEstatisticasPorData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        Map<String, Object> estatisticas = agendamentoService.getEstatisticasPorData(data);
        return ResponseEntity.ok(estatisticas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Agendamento> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(agendamentoService.buscarPorId(id));
    }

    @GetMapping("/profissional/{profissionalId}")
    public ResponseEntity<List<Agendamento>> listarPorProfissionalEPeriodo(
            @PathVariable Long profissionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        List<Agendamento> agendamentos = agendamentoService.listarAgendamentosPorProfissionalEPeriodo(profissionalId, inicio, fim);
        return ResponseEntity.ok(agendamentos);
    }
}