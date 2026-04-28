package com.example.agendamento_app.model;

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
@Table(name = "pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assinatura_id", nullable = false)
    private Long assinaturaId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(length = 20)
    private String status = "PENDENTE"; // PENDENTE, PAGO, FALHOU, REEMBOLSADO

    @Column(name = "metodo_pagamento", length = 30)
    private String metodoPagamento;

    @Column(name = "data_pagamento")
    private LocalDateTime dataPagamento;

    @Column(name = "data_vencimento")
    private LocalDateTime dataVencimento;

    @Column(name = "transacao_externa_id", length = 100)
    private String transacaoExternaId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}