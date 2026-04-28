// repository/AgendamentoRepository.java
package com.example.agendamento_app.repository;

import com.example.agendamento_app.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findByEmpresaId(Long empresaId);

    List<Agendamento> findByEmpresaIdAndDataHoraBetween(Long empresaId, LocalDateTime inicio, LocalDateTime fim);

    List<Agendamento> findByProfissionalIdAndDataHoraBetween(Long profissionalId, LocalDateTime inicio, LocalDateTime fim);

    // NOVO MÉTODO
    List<Agendamento> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT a FROM Agendamento a WHERE a.profissional.id = :profissionalId " +
            "AND a.status != 'CANCELADO' " +
            "AND a.status != 'CONCLUIDO'")
    List<Agendamento> findAgendamentosAtivosPorProfissional(@Param("profissionalId") Long profissionalId);

    @Query("SELECT a FROM Agendamento a WHERE a.empresa.id = :empresaId " +
            "AND a.dataHora BETWEEN :inicio AND :fim")
    List<Agendamento> findAgendamentosDoDia(@Param("empresaId") Long empresaId,
                                            @Param("inicio") LocalDateTime inicio,
                                            @Param("fim") LocalDateTime fim);
    long countByEmpresaId(Long empresaId);

}