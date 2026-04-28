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
@Table(name = "assinatura", indexes = {
        @Index(name = "idx_assinatura_empresa", columnList = "empresa_id"),
        @Index(name = "idx_assinatura_status", columnList = "status")
})
public class Assinatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    // ADICIONAR RELACIONAMENTO COM CLIENTE
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "plano_id", nullable = false)
    private Plano plano;

    @Column(nullable = false, length = 20)
    private String status = "ATIVA"; // ATIVA, CANCELADA, EXPIRADA, TESTE

    @Column(length = 10)
    private String periodo = "mensal"; // mensal, anual

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @Column(name = "data_cancelamento")
    private LocalDateTime dataCancelamento;

    @Column(name = "dias_teste")
    private Integer diasTeste = 7;

    @Column(name = "assinatura_externa_id", length = 100)
    private String assinaturaExternaId;

    @Column(name = "cliente_externo_id", length = 100)
    private String clienteExternoId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (dataInicio == null) {
            dataInicio = LocalDateTime.now();
        }
        if (status == null) {
            status = "ATIVA";
        }
        if (periodo == null) {
            periodo = "mensal";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}