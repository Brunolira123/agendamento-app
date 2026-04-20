package com.example.agendamento_app.repository;

import com.example.agendamento_app.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    // Busca serviços por empresa
    List<Servico> findByEmpresaId(Long empresaId);

    // Busca serviços ativos por empresa
    List<Servico> findByEmpresaIdAndAtivoTrue(Long empresaId);
}
