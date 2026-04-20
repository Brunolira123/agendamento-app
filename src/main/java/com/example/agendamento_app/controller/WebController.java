package com.example.agendamento_app.controller;

import com.example.agendamento_app.model.Usuario;
import com.example.agendamento_app.repository.ServicoRepository;
import com.example.agendamento_app.service.AgendamentoService;
import com.example.agendamento_app.service.AuthService;
import com.example.agendamento_app.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final AuthService authService;
    private final AgendamentoService agendamentoService;
    private final ProfissionalRepository profissionalRepository;
    private final ServicoRepository servicoRepository;

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) String data,
            @RequestParam(required = false) Long profissionalId,
            HttpSession session,
            Model model) {

        if (!authService.isLogado(session)) {
            return "redirect:/login";
        }

        Usuario usuario = authService.getUsuarioLogado(session);
        Long empresaId = usuario.getEmpresa().getId();

        LocalDate dataSelecionada = data != null ? LocalDate.parse(data) : LocalDate.now();

        var agendamentos = profissionalId != null && profissionalId > 0 ?
                agendamentoService.listarAgendamentosPorProfissionalEPeriodo(profissionalId, dataSelecionada, dataSelecionada) :
                agendamentoService.listarAgendamentosPorPeriodo(empresaId, dataSelecionada, dataSelecionada);

        var estatisticas = agendamentoService.getEstatisticas(empresaId, dataSelecionada);
        var profissionais = profissionalRepository.findByEmpresaId(empresaId);

        model.addAttribute("usuario", usuario);
        model.addAttribute("agendamentos", agendamentos);
        model.addAttribute("profissionais", profissionais);
        model.addAttribute("profissionalIdSelecionado", profissionalId);
        model.addAttribute("dataSelecionada", dataSelecionada.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        model.addAttribute("dataFormatada", dataSelecionada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.addAttribute("estatisticas", estatisticas);
        model.addAttribute("diaAnterior", dataSelecionada.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        model.addAttribute("diaSeguinte", dataSelecionada.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        return "dashboard";
    }

    // GET - Mostra o formulário (CORRETO)
    @GetMapping("/agendamento/novo")
    public String novoAgendamentoForm(Model model, HttpSession session) {
        if (!authService.isLogado(session)) {
            return "redirect:/login";
        }

        Usuario usuario = authService.getUsuarioLogado(session);
        Long empresaId = usuario.getEmpresa().getId();

        model.addAttribute("profissionais", profissionalRepository.findByEmpresaId(empresaId));
        model.addAttribute("servicos", servicoRepository.findByEmpresaId(empresaId));

        return "novo-agendamento";
    }

    // POST - Processa o formulário (ADICIONAR ESTE MÉTODO)
    @PostMapping("/agendamento/novo")
    public String novoAgendamentoSubmit(
            @RequestParam Long profissionalId,
            @RequestParam Long servicoId,
            @RequestParam String clienteNome,
            @RequestParam String clienteTelefone,
            @RequestParam String data,
            @RequestParam String hora,
            @RequestParam(required = false) String observacao,
            HttpSession session,
            Model model) {

        if (!authService.isLogado(session)) {
            return "redirect:/login";
        }

        Usuario usuario = authService.getUsuarioLogado(session);

        // Monta a data/hora
        LocalDateTime dataHora = LocalDateTime.parse(data + "T" + hora + ":00");

        // Cria o agendamento via API
        try {
            // Aqui você pode chamar o service diretamente
            // Ou fazer uma requisição interna
            model.addAttribute("sucesso", "Agendamento criado com sucesso!");
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("profissionais", profissionalRepository.findByEmpresaId(usuario.getEmpresa().getId()));
            model.addAttribute("servicos", servicoRepository.findByEmpresaId(usuario.getEmpresa().getId()));
            return "novo-agendamento";
        }
    }
}