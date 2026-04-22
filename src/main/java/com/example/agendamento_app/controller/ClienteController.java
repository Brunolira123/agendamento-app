// controller/ClienteController.java
package com.example.agendamento_app.controller;


import com.example.agendamento_app.DTO.ClienteRequestDTO;
import com.example.agendamento_app.DTO.ClienteResponseDTO;
import com.example.agendamento_app.mapper.ClienteMapper;
import com.example.agendamento_app.model.Cliente;
import com.example.agendamento_app.model.Empresa;
import com.example.agendamento_app.model.Usuario;
import com.example.agendamento_app.repository.ClienteRepository;
import com.example.agendamento_app.repository.EmpresaRepository;
import com.example.agendamento_app.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final ClienteMapper clienteMapper;
    private final AuthService authService;

    // Listar todos os clientes da empresa
    @GetMapping
    public ResponseEntity<?> listarClientes(HttpSession session,
                                            @PageableDefault(size = 20) Pageable pageable) {
        try {
            Usuario usuario = authService.getUsuarioLogado(session);

            System.out.println("Sessão ID: " + session.getId());
            System.out.println("Usuário na sessão: " + (usuario != null ? usuario.getEmail() : "null"));

            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado - Faça login novamente"));
            }

            Page<Cliente> clientes = clienteRepository.findByEmpresaId(usuario.getEmpresa().getId(), pageable);
            Page<ClienteResponseDTO> response = clientes.map(clienteMapper::toDto);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Buscar cliente por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarCliente(@PathVariable Long id, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Cliente cliente = clienteRepository.findById(id)
                    .orElse(null);

            if (cliente == null || !cliente.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
                return ResponseEntity.status(404).body(Map.of("error", "Cliente não encontrado"));
            }

            return ResponseEntity.ok(clienteMapper.toDto(cliente));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Buscar por telefone (útil no agendamento)
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPorTelefone(@RequestParam String telefone, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Cliente cliente = clienteRepository.findByEmpresaIdAndTelefone(usuario.getEmpresa().getId(), telefone)
                    .orElse(null);

            if (cliente == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Cliente não encontrado"));
            }

            return ResponseEntity.ok(clienteMapper.toDto(cliente));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Criar novo cliente
    @PostMapping
    public ResponseEntity<?> criarCliente(@Valid @RequestBody ClienteRequestDTO request,
                                          HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            // Verificar se telefone já existe
            if (request.getTelefone() != null && !request.getTelefone().isEmpty()) {
                clienteRepository.findByEmpresaIdAndTelefone(usuario.getEmpresa().getId(), request.getTelefone())
                        .ifPresent(c -> {
                            throw new RuntimeException("Telefone já cadastrado para outro cliente");
                        });
            }

            // Verificar se email já existe
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                clienteRepository.findByEmpresaIdAndEmail(usuario.getEmpresa().getId(), request.getEmail())
                        .ifPresent(c -> {
                            throw new RuntimeException("Email já cadastrado para outro cliente");
                        });
            }

            Cliente cliente = clienteMapper.toEntity(request, usuario.getEmpresa());
            cliente = clienteRepository.save(cliente);

            return ResponseEntity.status(HttpStatus.CREATED).body(clienteMapper.toDto(cliente));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Atualizar cliente
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarCliente(@PathVariable Long id,
                                              @Valid @RequestBody ClienteRequestDTO request,
                                              HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Cliente cliente = clienteRepository.findById(id)
                    .orElse(null);

            if (cliente == null || !cliente.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
                return ResponseEntity.status(404).body(Map.of("error", "Cliente não encontrado"));
            }

            // Atualizar dados
            cliente.setNome(request.getNome());
            cliente.setEmail(request.getEmail());
            cliente.setTelefone(request.getTelefone());
            cliente.setCpf(request.getCpf());
            cliente.setDataNascimento(request.getDataNascimento());
            cliente.setObservacao(request.getObservacao());

            cliente = clienteRepository.save(cliente);

            return ResponseEntity.ok(clienteMapper.toDto(cliente));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Ativar/Desativar cliente
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Cliente cliente = clienteRepository.findById(id)
                    .orElse(null);

            if (cliente == null || !cliente.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
                return ResponseEntity.status(404).body(Map.of("error", "Cliente não encontrado"));
            }

            cliente.setAtivo(!cliente.getAtivo());
            cliente = clienteRepository.save(cliente);

            Map<String, Object> response = new HashMap<>();
            response.put("id", cliente.getId());
            response.put("ativo", cliente.getAtivo());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Deletar cliente (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarCliente(@PathVariable Long id, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Cliente cliente = clienteRepository.findById(id)
                    .orElse(null);

            if (cliente == null || !cliente.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
                return ResponseEntity.status(404).body(Map.of("error", "Cliente não encontrado"));
            }

            clienteRepository.delete(cliente);

            return ResponseEntity.ok(Map.of("message", "Cliente removido com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}