// dto/ClienteResponseDTO.java
package com.example.agendamento_app.DTO;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ClienteResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String cpf;
    private LocalDate dataNascimento;
    private Integer pontosFidelidade;
    private Boolean ativo;
    private String observacao;
    private LocalDateTime createdAt;
    private Integer totalAgendamentos;
    private Boolean temAssinaturaAtiva;
}