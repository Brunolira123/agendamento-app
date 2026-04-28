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
@Table(name = "plano")
public class Plano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nome;

    @Column(unique = true, nullable = false, length = 50)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "preco_mensal", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoMensal;

    @Column(name = "preco_anual", precision = 10, scale = 2)
    private BigDecimal precoAnual;

    @Column(name = "max_profissionais")
    private Integer maxProfissionais;

    @Column(name = "max_agendamentos_mes")
    private Integer maxAgendamentosMes;

    @Column(name = "tem_app_mobile")
    private Boolean temAppMobile = false;

    @Column(name = "tem_relatorios")
    private Boolean temRelatorios = false;

    @Column(name = "tem_suporte_prioritario")
    private Boolean temSuportePrioritario = false;

    @Column(name = "tem_notificacao_whatsapp")
    private Boolean temNotificacaoWhatsapp = false;

    @Column(name = "tem_api")
    private Boolean temApi = false;

    private Integer ordem = 0;

    private Boolean ativo = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}