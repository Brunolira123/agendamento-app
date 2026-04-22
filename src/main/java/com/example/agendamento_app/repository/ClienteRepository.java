// repository/ClienteRepository.java
package com.example.agendamento_app.repository;

import com.example.agendamento_app.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Buscar por empresa
    List<Cliente> findByEmpresaId(Long empresaId);

    // Buscar por empresa com paginação
    Page<Cliente> findByEmpresaId(Long empresaId, Pageable pageable);

    // Buscar ativos por empresa
    List<Cliente> findByEmpresaIdAndAtivoTrue(Long empresaId);

    // Buscar por telefone (útil para agendamento)
    Optional<Cliente> findByEmpresaIdAndTelefone(Long empresaId, String telefone);

    // Buscar por email
    Optional<Cliente> findByEmpresaIdAndEmail(Long empresaId, String email);

    // Buscar por nome (like)
    List<Cliente> findByEmpresaIdAndNomeContainingIgnoreCase(Long empresaId, String nome);

    // Buscar clientes que mais agendam
    @Query("SELECT c, COUNT(a) as totalAgendamentos " +
            "FROM Cliente c LEFT JOIN Agendamento a ON a.cliente = c " +
            "WHERE c.empresa.id = :empresaId " +
            "GROUP BY c.id " +
            "ORDER BY totalAgendamentos DESC")
    List<Object[]> findTopClientesByAgendamentos(@Param("empresaId") Long empresaId, Pageable pageable);

    // Buscar clientes com assinatura ativa
    @Query("SELECT DISTINCT c FROM Cliente c " +
            "JOIN c.assinaturas a " +
            "WHERE c.empresa.id = :empresaId " +
            "AND a.status = 'ATIVA' " +
            "AND a.dataFim > CURRENT_DATE")
    List<Cliente> findClientesComAssinaturaAtiva(@Param("empresaId") Long empresaId);
}