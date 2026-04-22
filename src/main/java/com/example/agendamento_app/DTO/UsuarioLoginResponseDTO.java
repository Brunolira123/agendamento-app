package com.example.agendamento_app.DTO;
import lombok.Data;

@Data
public class UsuarioLoginResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private Long empresaId;
    private String papel;
    private Boolean ativo;
}