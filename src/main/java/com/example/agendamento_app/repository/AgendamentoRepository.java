package com.example.agendamento_app.repository;

import com.example.agendamento_app.model.Agendamento;
import com.example.agendamento_app.model.enums.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    // Busca agendamentos por empresa
    List<Agendamento> findByEmpresaId(Long empresaId);

    // Busca agendamentos por empresa e período (MAIS IMPORTANTE - ESTAVA FALTANDO)
    List<Agendamento> findByEmpresaIdAndDataHoraBetween(
            Long empresaId,
            LocalDateTime inicio,
            LocalDateTime fim
    );

    // Busca agendamentos do dia
    @Query("SELECT a FROM Agendamento a WHERE a.empresa.id = :empresaId " +
            "AND a.dataHora BETWEEN :inicio AND :fim " +
            "ORDER BY a.dataHora")
    List<Agendamento> findAgendamentosDoDia(
            @Param("empresaId") Long empresaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    // Busca agendamentos por profissional em um período
    List<Agendamento> findByProfissionalIdAndDataHoraBetween(
            Long profissionalId,
            LocalDateTime inicio,
            LocalDateTime fim
    );

    // Busca agendamentos por profissional e status
    List<Agendamento> findByProfissionalIdAndStatusNot(
            Long profissionalId,
            StatusAgendamento status
    );

    // Busca por status
    List<Agendamento> findByEmpresaIdAndStatus(Long empresaId, StatusAgendamento status);

    // Busca agendamentos ativos por profissional (não cancelados)
    @Query("SELECT a FROM Agendamento a WHERE a.profissional.id = :profissionalId " +
            "AND a.status != 'CANCELADO'")
    List<Agendamento> findAgendamentosAtivosPorProfissional(@Param("profissionalId") Long profissionalId);
}