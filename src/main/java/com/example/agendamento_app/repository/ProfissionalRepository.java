package com.example.agendamento_app.repository;

import com.example.agendamento_app.model.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    // Busca profissionais por empresa
    List<Profissional> findByEmpresaId(Long empresaId);

    // Busca profissionais ativos por empresa
    List<Profissional> findByEmpresaIdAndAtivoTrue(Long empresaId);

    long countByEmpresaId(Long empresaId);
}
