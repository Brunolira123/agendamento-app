package com.example.agendamento_app.repository;

import com.example.agendamento_app.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailAndSenha(String email, String senha);

    Optional<Usuario> findByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE u.empresa.id = :empresaId AND u.papel = :papel")
    Usuario findByEmpresaIdAndPapel(@Param("empresaId") Long empresaId, @Param("papel") String papel);
}