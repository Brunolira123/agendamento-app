package com.example.agendamento_app.repository;

import com.example.agendamento_app.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findAllByOrderByCreatedAtDesc();

    List<Pagamento> findByDataPagamentoBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p WHERE p.status = 'PAGO' AND p.dataPagamento BETWEEN :inicio AND :fim")
    BigDecimal sumValorByDataPagamentoBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    List<Pagamento> findByAssinaturaId(Long assinaturaId);

    // CORRIGIDO: Usando JOIN com Assinatura
    @Query("SELECT p FROM Pagamento p WHERE p.assinaturaId IN (SELECT a.id FROM Assinatura a WHERE a.empresa.id = :empresaId) ORDER BY p.createdAt DESC")
    List<Pagamento> findByAssinaturaEmpresaIdOrderByCreatedAtDesc(@Param("empresaId") Long empresaId);
}