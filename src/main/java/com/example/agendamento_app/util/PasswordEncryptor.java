// util/PasswordEncryptor.java
package com.example.agendamento_app.util;

import com.example.agendamento_app.model.Usuario;
import com.example.agendamento_app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncryptor implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Buscar todos os usuários
        var usuarios = usuarioRepository.findAll();
        boolean atualizou = false;

        for (Usuario usuario : usuarios) {
            String senhaAtual = usuario.getSenha();

            // Verificar se a senha já está em BCrypt
            if (!senhaAtual.startsWith("$2a$")) {
                String novaSenha = encoder.encode(senhaAtual);
                usuario.setSenha(novaSenha);
                usuarioRepository.save(usuario);
                System.out.println("Senha atualizada para: " + usuario.getEmail());
                atualizou = true;
            }
        }

        if (!atualizou) {
            System.out.println("Todas as senhas já estão criptografadas!");
        }
    }
}