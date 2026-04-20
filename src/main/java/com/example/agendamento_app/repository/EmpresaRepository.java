package com.example.agendamento_app.repository;

import com.example.agendamento_app.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
}
