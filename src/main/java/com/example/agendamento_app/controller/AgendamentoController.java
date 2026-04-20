package com.example.agendamento_app.controller;

import com.example.agendamento_app.model.Agendamento;
import com.example.agendamento_app.model.enums.StatusAgendamento;
import com.example.agendamento_app.service.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    @PostMapping
    public ResponseEntity<Agendamento> criar(@Valid @RequestBody Agendamento agendamento) {
        Agendamento novo = agendamentoService.criarAgendamento(agendamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    // CORRIGIDO - Aceita GET (mais fácil para links)
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

    @GetMapping("/empresa/{empresaId}/hoje")
    public ResponseEntity<List<Agendamento>> agendamentosHoje(@PathVariable Long empresaId) {
        return ResponseEntity.ok(agendamentoService.listarAgendamentosDoDia(empresaId));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<Agendamento>> listarTodos(@PathVariable Long empresaId) {
        return ResponseEntity.ok(agendamentoService.listarTodosPorEmpresa(empresaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Agendamento> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(agendamentoService.buscarPorId(id));
    }
}