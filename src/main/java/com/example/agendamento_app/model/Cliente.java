// model/Cliente.java
package com.example.agendamento_app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cliente", indexes = {
        @Index(name = "idx_cliente_empresa", columnList = "empresa_id"),
        @Index(name = "idx_cliente_telefone", columnList = "telefone"),
        @Index(name = "idx_cliente_email", columnList = "email")
})
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(length = 14)
    private String cpf;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "pontos_fidelidade")
    private Integer pontosFidelidade = 0;

    private Boolean ativo = true;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "cliente")
    private List<Assinatura> assinaturas = new ArrayList<>();

    @OneToMany(mappedBy = "cliente")
    private List<Agendamento> agendamentos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (pontosFidelidade == null) pontosFidelidade = 0;
        if (ativo == null) ativo = true;
    }
}