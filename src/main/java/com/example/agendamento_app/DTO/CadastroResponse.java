package com.example.agendamento_app.DTO;

import lombok.Data;
import java.util.Map;

@Data
public class CadastroResponse {
    private String token;
    private Map<String, Object> usuario;
    private Map<String, Object> empresa;
}