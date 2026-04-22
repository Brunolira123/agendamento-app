package com.example.agendamento_app.model;

import com.example.agendamento_app.model.enums.StatusAgendamento;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "agendamento", indexes = {
        @Index(name = "idx_empresa_data", columnList = "empresa_id, data_hora"),
        @Index(name = "idx_profissional_data", columnList = "profissional_id, data_hora"),
        @Index(name = "idx_cliente_id", columnList = "cliente_id")
})
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @ManyToOne
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    // NOVO: Relacionamento com Cliente
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // Mantido para compatibilidade (clientes avulsos)
    @Column(name = "cliente_nome", length = 100)
    private String clienteNome;

    @Column(name = "cliente_telefone", length = 20)
    private String clienteTelefone;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAgendamento status = StatusAgendamento.AGENDADO;

    @Column(name = "preco_cobrado", precision = 10, scale = 2)
    private BigDecimal precoCobrado;

    // NOVO: Flag para saber se usou assinatura
    @Column(name = "usou_assinatura")
    private Boolean usouAssinatura = false;

    // NOVO: Referência à assinatura usada
    @ManyToOne
    @JoinColumn(name = "assinatura_id")
    private Assinatura assinatura;

    // NOVO: Comissão do profissional para este serviço
    @Column(name = "comissao_profissional", precision = 10, scale = 2)
    private BigDecimal comissaoProfissional;

    // NOVO: Flag se a comissão já foi paga
    @Column(name = "comissao_paga")
    private Boolean comissaoPaga = false;

    // NOVO: Valor original do serviço (antes de desconto/plano)
    @Column(name = "preco_original", precision = 10, scale = 2)
    private BigDecimal precoOriginal;

    private String observacao;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();

        // Se tem cliente, usa nome do cliente
        if (cliente != null && clienteNome == null) {
            clienteNome = cliente.getNome();
            clienteTelefone = cliente.getTelefone();
        }

        // Define preços
        if (precoCobrado == null && servico != null) {
            precoOriginal = servico.getPreco();
            precoCobrado = servico.getPreco();
        }

        if (duracaoMinutos == null && servico != null) {
            duracaoMinutos = servico.getDuracaoMinutos();
        }

        // Defaults
        if (usouAssinatura == null) usouAssinatura = false;
        if (comissaoPaga == null) comissaoPaga = false;
    }
}