package com.example.agendamento_app.repository;

import com.example.agendamento_app.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findBySlug(String slug);

    long countByCreatedAtAfter(LocalDateTime data);

    @Query("SELECT e FROM Empresa e WHERE LOWER(e.nome) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Empresa> findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(@Param("search") String search, @Param("search") String search2);
}