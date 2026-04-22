package com.example.agendamento_app.model;

import com.example.agendamento_app.model.enums.StatusAssinatura;
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
@Table(name = "assinatura", indexes = {
        @Index(name = "idx_assinatura_cliente", columnList = "cliente_id"),
        @Index(name = "idx_assinatura_status", columnList = "status")
})
public class Assinatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "plano_id", nullable = false)
    private PlanoAssinatura plano;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "atendimentos_utilizados_mes")
    private Integer atendimentosUtilizadosMes = 0;

    @Column(name = "ultima_renovacao")
    private LocalDate ultimaRenovacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAssinatura status = StatusAssinatura.ATIVA;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "assinatura")
    private List<Agendamento> agendamentos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (atendimentosUtilizadosMes == null) atendimentosUtilizadosMes = 0;
        if (status == null) status = StatusAssinatura.ATIVA;

        // Se não tem data fim, define para 1 mês após início
        if (dataFim == null && dataInicio != null) {
            dataFim = dataInicio.plusMonths(1);
        }
    }
}

