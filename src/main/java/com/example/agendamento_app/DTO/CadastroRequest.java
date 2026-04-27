package com.example.agendamento_app.DTO;

import lombok.Data;

@Data
public class CadastroRequest {
    private String nomeEmpresa;
    private String slug;
    private String nicho;
    private String email;
    private String telefone;
    private String nomeDono;
    private String senha;
}