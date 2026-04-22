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
@Table(name = "plano_assinatura")
public class PlanoAssinatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precoMensal;

    @Column(name = "limite_atendimentos_mes")
    private Integer limiteAtendimentosMes;

    @Column(name = "servicos_incluidos", columnDefinition = "TEXT")
    private String servicosIncluidosJson; // ["Corte", "Barba"]

    @Column(name = "desconto_percentual")
    private Integer descontoPercentual;

    private Boolean ativo = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (ativo == null) ativo = true;
        if (descontoPercentual == null) descontoPercentual = 0;
    }
}