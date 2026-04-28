package com.example.agendamento_app.repository;

import com.example.agendamento_app.model.Assinatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AssinaturaRepository extends JpaRepository<Assinatura, Long> {

    Optional<Assinatura> findByEmpresaIdAndStatus(Long empresaId, String status);

    List<Assinatura> findByEmpresaId(Long empresaId);

    List<Assinatura> findByStatus(String status);

    List<Assinatura> findByEmpresaIdOrderByCreatedAtDesc(Long empresaId);

    long countByStatus(String status);

    @Query("SELECT a FROM Assinatura a WHERE a.dataFim BETWEEN :inicio AND :fim AND a.status = :status")
    List<Assinatura> findByDataFimBetweenAndStatus(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim, @Param("status") String status);

    @Query("SELECT a.plano.nome, COUNT(a), SUM(a.valor) FROM Assinatura a GROUP BY a.plano.id, a.plano.nome")
    List<Object[]> countByPlanoGroup();
}