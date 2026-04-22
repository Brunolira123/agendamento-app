// service/AgendamentoService.java
package com.example.agendamento_app.service;

import com.example.agendamento_app.model.Agendamento;
import com.example.agendamento_app.model.Profissional;
import com.example.agendamento_app.model.Servico;
import com.example.agendamento_app.model.enums.StatusAgendamento;
import com.example.agendamento_app.exception.RegraNegocioException;
import com.example.agendamento_app.exception.ResourceNotFoundException;
import com.example.agendamento_app.repository.AgendamentoRepository;
import com.example.agendamento_app.repository.ProfissionalRepository;
import com.example.agendamento_app.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ServicoRepository servicoRepository;

    @Transactional
    public Agendamento criarAgendamento(Agendamento agendamento) {
        Profissional profissional = profissionalRepository.findById(agendamento.getProfissional().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));

        if (!profissional.getAtivo()) {
            throw new RegraNegocioException("Profissional está inativo");
        }

        Servico servico = servicoRepository.findById(agendamento.getServico().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));

        if (!servico.getAtivo()) {
            throw new RegraNegocioException("Serviço está inativo");
        }

        LocalDateTime inicio = agendamento.getDataHora();
        if (inicio.isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("Não é possível agendar no passado");
        }

        LocalDateTime fim = inicio.plusMinutes(servico.getDuracaoMinutos());
        validarHorarioDisponivel(profissional.getId(), inicio, fim);

        agendamento.setProfissional(profissional);
        agendamento.setServico(servico);
        agendamento.setDuracaoMinutos(servico.getDuracaoMinutos());
        agendamento.setPrecoCobrado(servico.getPreco());
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        agendamento.setEmpresa(profissional.getEmpresa());

        return agendamentoRepository.save(agendamento);
    }

    private void validarHorarioDisponivel(Long profissionalId, LocalDateTime inicio, LocalDateTime fim) {
        List<Agendamento> agendamentosExistentes = agendamentoRepository
                .findAgendamentosAtivosPorProfissional(profissionalId);

        for (Agendamento existente : agendamentosExistentes) {
            LocalDateTime inicioExistente = existente.getDataHora();
            LocalDateTime fimExistente = inicioExistente.plusMinutes(existente.getDuracaoMinutos());

            if (inicio.isBefore(fimExistente) && fim.isAfter(inicioExistente)) {
                throw new RegraNegocioException("Horário já ocupado para este profissional");
            }
        }
    }

    @Transactional
    public Agendamento atualizarStatus(Long id, StatusAgendamento novoStatus) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + id));

        if (novoStatus == null) {
            throw new RegraNegocioException("Status não informado");
        }

        if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new RegraNegocioException("Agendamento cancelado não pode ser alterado");
        }

        if (agendamento.getStatus() == StatusAgendamento.CONCLUIDO) {
            throw new RegraNegocioException("Agendamento concluído não pode ser alterado");
        }

        agendamento.setStatus(novoStatus);
        return agendamentoRepository.save(agendamento);
    }

    public List<Agendamento> listarAgendamentosDoDia(Long empresaId) {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fim = LocalDate.now().plusDays(1).atStartOfDay();
        return agendamentoRepository.findByEmpresaIdAndDataHoraBetween(empresaId, inicio, fim);
    }

    // NOVO MÉTODO
    public List<Agendamento> listarPorData(LocalDateTime inicio, LocalDateTime fim) {
        return agendamentoRepository.findByDataHoraBetween(inicio, fim);
    }

    public List<Agendamento> listarAgendamentosPorPeriodo(Long empresaId, LocalDate inicio, LocalDate fim) {
        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime fimDateTime = fim.plusDays(1).atStartOfDay();
        return agendamentoRepository.findByEmpresaIdAndDataHoraBetween(empresaId, inicioDateTime, fimDateTime);
    }

    public List<Agendamento> listarAgendamentosPorProfissionalEPeriodo(Long profissionalId, LocalDate inicio, LocalDate fim) {
        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime fimDateTime = fim.plusDays(1).atStartOfDay();
        return agendamentoRepository.findByProfissionalIdAndDataHoraBetween(profissionalId, inicioDateTime, fimDateTime);
    }

    // NOVO MÉTODO - Versão simplificada para o Dashboard
    public Map<String, Object> getEstatisticasPorData(LocalDate data) {
        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.plusDays(1).atStartOfDay();

        List<Agendamento> agendamentos = agendamentoRepository.findByDataHoraBetween(inicio, fim);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", agendamentos.size());
        stats.put("confirmados", agendamentos.stream().filter(a -> a.getStatus() == StatusAgendamento.CONFIRMADO).count());
        stats.put("concluidos", agendamentos.stream().filter(a -> a.getStatus() == StatusAgendamento.CONCLUIDO).count());
        stats.put("cancelados", agendamentos.stream().filter(a -> a.getStatus() == StatusAgendamento.CANCELADO).count());
        stats.put("faturamento", agendamentos.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.CONCLUIDO)
                .mapToDouble(a -> a.getPrecoCobrado().doubleValue())
                .sum());

        return stats;
    }

    public Map<String, Object> getEstatisticas(Long empresaId, LocalDate data) {
        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.plusDays(1).atStartOfDay();

        List<Agendamento> agendamentos = agendamentoRepository.findByEmpresaIdAndDataHoraBetween(empresaId, inicio, fim);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", agendamentos.size());
        stats.put("confirmados", agendamentos.stream().filter(a -> a.getStatus() == StatusAgendamento.CONFIRMADO).count());
        stats.put("concluidos", agendamentos.stream().filter(a -> a.getStatus() == StatusAgendamento.CONCLUIDO).count());
        stats.put("cancelados", agendamentos.stream().filter(a -> a.getStatus() == StatusAgendamento.CANCELADO).count());
        stats.put("faturamento", agendamentos.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.CONCLUIDO)
                .mapToDouble(a -> a.getPrecoCobrado().doubleValue())
                .sum());

        return stats;
    }

    public Agendamento buscarPorId(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + id));
    }

    public List<Agendamento> listarTodosPorEmpresa(Long empresaId) {
        return agendamentoRepository.findByEmpresaId(empresaId);
    }
}