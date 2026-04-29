package com.example.agendamento_app.controller;

import com.example.agendamento_app.model.Empresa;
import com.example.agendamento_app.model.Usuario;
import com.example.agendamento_app.model.Plano;
import com.example.agendamento_app.model.Assinatura;
import com.example.agendamento_app.model.UsoEmpresa;
import com.example.agendamento_app.repository.EmpresaRepository;
import com.example.agendamento_app.repository.UsuarioRepository;
import com.example.agendamento_app.repository.PlanoRepository;
import com.example.agendamento_app.repository.AssinaturaRepository;
import com.example.agendamento_app.repository.UsoEmpresaRepository;
import com.example.agendamento_app.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final PlanoRepository planoRepository;
    private final AssinaturaRepository assinaturaRepository;
    private final UsoEmpresaRepository usoEmpresaRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Método para validar CPF
    private boolean validarCPF(String cpf) {
        if (cpf == null || cpf.isEmpty()) return true; // CPF é opcional

        String cpfLimpo = cpf.replaceAll("\\D", "");
        if (cpfLimpo.length() != 11) return false;

        // Verificar se todos os dígitos são iguais
        if (cpfLimpo.matches("(\\d)\\1{10}")) return false;

        // Calcular primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += (cpfLimpo.charAt(i) - '0') * (10 - i);
        }
        int primeiroDigito = 11 - (soma % 11);
        if (primeiroDigito >= 10) primeiroDigito = 0;

        if (primeiroDigito != (cpfLimpo.charAt(9) - '0')) return false;

        // Calcular segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += (cpfLimpo.charAt(i) - '0') * (11 - i);
        }
        int segundoDigito = 11 - (soma % 11);
        if (segundoDigito >= 10) segundoDigito = 0;

        return segundoDigito == (cpfLimpo.charAt(10) - '0');
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String senha) {
        System.out.println("🔐 Tentativa de login: " + email);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, senha)
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("id", usuario.getId());
            response.put("nome", usuario.getNome());
            response.put("email", usuario.getEmail());
            response.put("papel", usuario.getPapel());
            response.put("empresaId", usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null);

            System.out.println("✅ Login realizado com sucesso: " + email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ Erro no login: " + e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Credenciais inválidas"));
        }
    }

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(@RequestBody Map<String, Object> request) {
        System.out.println("📝 Cadastrando nova empresa: " + request.get("email"));

        try {
            String email = (String) request.get("email");
            String senha = (String) request.get("senha");
            String nomeEmpresa = (String) request.get("nomeEmpresa");
            String slug = (String) request.get("slug");
            String nicho = (String) request.get("nicho");
            String telefone = (String) request.get("telefone");
            String nomeDono = (String) request.get("nomeDono");
            String cpf = (String) request.get("cpf");

            // Validar CPF se foi informado
            if (cpf != null && !cpf.isEmpty() && !validarCPF(cpf)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "CPF inválido"));
            }

            // Validar email
            if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "E-mail inválido"));
            }

            // Pegar plano (padrão: Profissional = 2)
            Long planoId = request.get("planoId") != null ?
                    Long.valueOf(request.get("planoId").toString()) : 2L;
            String periodo = request.get("periodo") != null ?
                    request.get("periodo").toString() : "mensal";

            // Validações básicas
            if (email == null || senha == null || nomeEmpresa == null || slug == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Campos obrigatórios não preenchidos"));
            }

            // 1. Verifica se email já existe
            if (usuarioRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "E-mail já cadastrado"));
            }

            // 2. Verifica se slug já existe
            if (empresaRepository.findBySlug(slug).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "URL já está em uso. Escolha outra."));
            }

            // 3. Busca o plano
            Plano plano = planoRepository.findById(planoId)
                    .orElseThrow(() -> new RuntimeException("Plano não encontrado"));

            // 4. Cria a empresa
            Empresa empresa = new Empresa();
            empresa.setNome(nomeEmpresa);
            empresa.setSlug(slug);
            empresa.setNicho(nicho != null ? nicho : "barbearia");
            empresa.setEmail(email);
            empresa.setTelefone(telefone);
            empresa.setCpfProprietario(cpf != null ? cpf.replaceAll("\\D", "") : null);
            empresa.setConfig("{}");
            Empresa empresaSalva = empresaRepository.save(empresa);
            System.out.println("✅ Empresa criada: ID=" + empresaSalva.getId());

            // 5. Cria o usuário dono
            Usuario usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setSenha(passwordEncoder.encode(senha));
            usuario.setNome(nomeDono != null ? nomeDono : "Proprietário");
            usuario.setEmpresa(empresaSalva);
            usuario.setPapel("DONO");
            usuario.setAtivo(true);
            Usuario usuarioSalvo = usuarioRepository.save(usuario);
            System.out.println("✅ Usuário criado: ID=" + usuarioSalvo.getId());

            // 6. Cria a assinatura (teste grátis de 7 dias)
            Assinatura assinatura = new Assinatura();
            assinatura.setEmpresa(empresaSalva);
            assinatura.setPlano(plano);
            assinatura.setStatus("TESTE");
            assinatura.setPeriodo(periodo);

            BigDecimal valor = periodo.equals("anual") ? plano.getPrecoAnual() : plano.getPrecoMensal();
            assinatura.setValor(valor);
            assinatura.setDataInicio(LocalDateTime.now());
            assinatura.setDataFim(LocalDateTime.now().plusDays(7));
            assinatura.setDiasTeste(7);

            Assinatura assinaturaSalva = assinaturaRepository.save(assinatura);
            System.out.println("✅ Assinatura criada: ID=" + assinaturaSalva.getId());

            // 7. Cria registro de uso da empresa
            LocalDate primeiroDiaMes = LocalDate.now().withDayOfMonth(1);
            UsoEmpresa usoEmpresa = new UsoEmpresa();
            usoEmpresa.setEmpresa(empresaSalva);
            usoEmpresa.setMesReferencia(primeiroDiaMes);
            usoEmpresa.setTotalAgendamentos(0);
            usoEmpresa.setTotalProfissionais(0);
            usoEmpresa.setAgendamentosLimite(plano.getMaxAgendamentosMes());
            usoEmpresa.setProfissionaisLimite(plano.getMaxProfissionais());
            usoEmpresaRepository.save(usoEmpresa);
            System.out.println("✅ Uso empresa criado: ID=" + usoEmpresa.getId());

            // 8. Gera token JWT
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(usuarioSalvo.getEmail())
                    .password(usuarioSalvo.getSenha())
                    .authorities("USER")
                    .build();
            String token = jwtService.generateToken(userDetails);

            // 9. Monta resposta
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);

            Map<String, Object> usuarioMap = new HashMap<>();
            usuarioMap.put("id", usuarioSalvo.getId());
            usuarioMap.put("nome", usuarioSalvo.getNome());
            usuarioMap.put("email", usuarioSalvo.getEmail());
            usuarioMap.put("papel", usuarioSalvo.getPapel());
            usuarioMap.put("empresaId", empresaSalva.getId());
            response.put("usuario", usuarioMap);

            Map<String, Object> empresaMap = new HashMap<>();
            empresaMap.put("id", empresaSalva.getId());
            empresaMap.put("nome", empresaSalva.getNome());
            empresaMap.put("slug", empresaSalva.getSlug());
            empresaMap.put("nicho", empresaSalva.getNicho());
            response.put("empresa", empresaMap);

            Map<String, Object> assinaturaMap = new HashMap<>();
            assinaturaMap.put("id", assinaturaSalva.getId());
            assinaturaMap.put("plano", plano.getNome());
            assinaturaMap.put("status", assinaturaSalva.getStatus());
            assinaturaMap.put("diasTeste", assinaturaSalva.getDiasTeste());
            assinaturaMap.put("dataFim", assinaturaSalva.getDataFim());
            response.put("assinatura", assinaturaMap);

            System.out.println("✅ Cadastro completo para: " + email);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("❌ Erro no cadastro: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno ao cadastrar: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-admin")
    public String resetAdmin() {
        // Remove empresa do admin se existir
        empresaRepository.findBySlug("barbearia-teste").ifPresent(e -> {
            empresaRepository.delete(e);
            System.out.println("Empresa antiga removida");
        });

        // Remove admin existente
        usuarioRepository.findByEmail("admin@barbearia.com").ifPresent(u -> {
            usuarioRepository.delete(u);
            System.out.println("Admin antigo removido");
        });

        // Cria empresa
        Empresa empresa = new Empresa();
        empresa.setNome("Barbearia Teste");
        empresa.setSlug("barbearia-teste");
        empresa.setNicho("barbearia");
        empresa.setEmail("admin@barbearia.com");
        empresa.setTelefone("11999999999");
        empresa.setConfig("{}");
        Empresa empresaSalva = empresaRepository.save(empresa);

        // Cria novo admin
        Usuario usuario = new Usuario();
        usuario.setEmail("admin@barbearia.com");
        usuario.setSenha(passwordEncoder.encode("123456"));
        usuario.setNome("Administrador");
        usuario.setEmpresa(empresaSalva);
        usuario.setPapel("ADMIN");
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);

        return "✅ Admin e empresa criados com sucesso!";
    }
}