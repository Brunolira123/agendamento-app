// service/AuthService.java
package com.example.agendamento_app.service;

import com.example.agendamento_app.model.Usuario;
import com.example.agendamento_app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    public boolean login(String email, String senha, HttpSession session) {
        System.out.println("AuthService.login - Email: " + email);
        System.out.println("AuthService.login - Sessão recebida: " + session.getId());

        Optional<Usuario> usuario = usuarioRepository.findByEmailAndSenha(email, senha);

        if (usuario.isPresent()) {
            session.setAttribute("usuarioLogado", usuario.get());
            session.setMaxInactiveInterval(3600); // 1 hora

            System.out.println("Usuário salvo na sessão: " + usuario.get().getEmail());
            System.out.println("Sessão após salvar: " + session.getId());
            return true;
        }

        System.out.println("Usuário não encontrado para: " + email);
        return false;
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    public Usuario getUsuarioLogado(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        System.out.println("Buscando usuário na sessão " + session.getId() + ": " + (usuario != null ? usuario.getEmail() : "null"));
        return usuario;
    }

    public boolean isLogado(HttpSession session) {
        return getUsuarioLogado(session) != null;
    }
}