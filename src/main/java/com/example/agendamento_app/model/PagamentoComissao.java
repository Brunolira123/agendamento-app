// PagamentoComissao.java
package com.example.agendamento_app.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@Entity
@Table(name = "pagamento_comissao")
public class PagamentoComissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profissional_id")
    private Profissional profissional;

    @Column(name = "periodo_referencia")
    private LocalDate periodoReferencia; // Mês/Ano

    @Column(name = "valor_total")
    private BigDecimal valorTotal;

    private Boolean pago = false;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    private String observacao;
}