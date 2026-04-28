package com.example.agendamento_app.repository;

import com.example.agendamento_app.model.Plano;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlanoRepository extends JpaRepository<Plano, Long> {
    Optional<Plano> findBySlug(String slug);
    List<Plano> findByAtivoTrueOrderByOrdemAsc();
}