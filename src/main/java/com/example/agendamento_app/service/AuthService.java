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
        Optional<Usuario> usuario = usuarioRepository.findByEmailAndSenha(email, senha);

        if (usuario.isPresent()) {
            session.setAttribute("usuarioLogado", usuario.get());
            return true;
        }
        return false;
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    public Usuario getUsuarioLogado(HttpSession session) {
        return (Usuario) session.getAttribute("usuarioLogado");
    }

    public boolean isLogado(HttpSession session) {
        return getUsuarioLogado(session) != null;
    }
}