// dto/ClienteRequestDTO.java
package com.example.agendamento_app.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ClienteRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @Email(message = "Email inválido")
    private String email;

    @Pattern(regexp = "^\\(?[1-9]{2}\\)? ?(?:[2-8]|9[1-9])[0-9]{3}\\-?[0-9]{4}$",
            message = "Telefone inválido")
    private String telefone;

    @Pattern(regexp = "\\d{3}\\.?\\d{3}\\.?\\d{3}\\-?\\d{2}",
            message = "CPF inválido")
    private String cpf;

    private LocalDate dataNascimento;
    private String observacao;
}