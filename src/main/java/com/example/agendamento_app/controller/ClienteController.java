// controller/ClienteController.java - versão corrigida
package com.example.agendamento_app.controller;


import com.example.agendamento_app.DTO.ClienteRequestDTO;
import com.example.agendamento_app.DTO.ClienteResponseDTO;
import com.example.agendamento_app.mapper.ClienteMapper;
import com.example.agendamento_app.model.Cliente;
import com.example.agendamento_app.model.Usuario;
import com.example.agendamento_app.repository.ClienteRepository;
import com.example.agendamento_app.repository.EmpresaRepository;
import com.example.agendamento_app.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteMapper clienteMapper;

    // Obter usuário logado do SecurityContext (JWT)
    private Usuario getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email = authentication.getName();
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    @GetMapping
    public ResponseEntity<?> listarClientes(@PageableDefault(size = 20) Pageable pageable) {
        try {
            Usuario usuario = getUsuarioLogado();

            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            System.out.println("Usuário logado: " + usuario.getEmail());
            System.out.println("Empresa: " + (usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : "null"));

            // Verificar se usuário tem empresa
            if (usuario.getEmpresa() == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Usuário não está associado a nenhuma empresa. Contate o administrador."));
            }

            Page<Cliente> clientes = clienteRepository.findByEmpresaId(usuario.getEmpresa().getId(), pageable);
            Page<ClienteResponseDTO> response = clientes.map(clienteMapper::toDto);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarCliente(@PathVariable Long id) {
        try {
            Usuario usuario = getUsuarioLogado();
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Cliente cliente = clienteRepository.findById(id).orElse(null);

            if (cliente == null || !cliente.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
                return ResponseEntity.status(404).body(Map.of("error", "Cliente não encontrado"));
            }

            return ResponseEntity.ok(clienteMapper.toDto(cliente));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPorTelefone(@RequestParam String telefone) {
        try {
            Usuario usuario = getUsuarioLogado();
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

    @PostMapping
    public ResponseEntity<?> criarCliente(@Valid @RequestBody ClienteRequestDTO request) {
        try {
            Usuario usuario = getUsuarioLogado();
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            // Verificar se telefone já existe
            if (request.getTelefone() != null && !request.getTelefone().isEmpty()) {
                if (clienteRepository.findByEmpresaIdAndTelefone(usuario.getEmpresa().getId(), request.getTelefone()).isPresent()) {
                    return ResponseEntity.status(400).body(Map.of("error", "Telefone já cadastrado"));
                }
            }

            // Verificar se email já existe
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                // Aqui você pode adicionar verificação de email se tiver o método
            }

            Cliente cliente = clienteMapper.toEntity(request, usuario.getEmpresa());
            cliente = clienteRepository.save(cliente);

            return ResponseEntity.status(HttpStatus.CREATED).body(clienteMapper.toDto(cliente));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarCliente(@PathVariable Long id, @Valid @RequestBody ClienteRequestDTO request) {
        try {
            Usuario usuario = getUsuarioLogado();
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Cliente cliente = clienteRepository.findById(id).orElse(null);

            if (cliente == null || !cliente.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
                return ResponseEntity.status(404).body(Map.of("error", "Cliente não encontrado"));
            }

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

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        try {
            Usuario usuario = getUsuarioLogado();
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Cliente cliente = clienteRepository.findById(id).orElse(null);

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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarCliente(@PathVariable Long id) {
        try {
            Usuario usuario = getUsuarioLogado();
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Não autorizado"));
            }

            Cliente cliente = clienteRepository.findById(id).orElse(null);

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