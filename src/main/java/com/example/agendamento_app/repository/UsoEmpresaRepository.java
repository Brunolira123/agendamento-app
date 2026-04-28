package com.example.agendamento_app.repository;

import com.example.agendamento_app.model.UsoEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface UsoEmpresaRepository extends JpaRepository<UsoEmpresa, Long> {
    Optional<UsoEmpresa> findByEmpresaIdAndMesReferencia(Long empresaId, LocalDate mesReferencia);
}