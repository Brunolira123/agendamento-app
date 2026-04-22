// mapper/ClienteMapper.java
package com.example.agendamento_app.mapper;

import com.example.agendamento_app.DTO.ClienteRequestDTO;
import com.example.agendamento_app.DTO.ClienteResponseDTO;
import com.example.agendamento_app.model.Cliente;
import com.example.agendamento_app.model.Empresa;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteRequestDTO dto, Empresa empresa) {
        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefone(dto.getTelefone());
        cliente.setCpf(dto.getCpf());
        cliente.setDataNascimento(dto.getDataNascimento());
        cliente.setObservacao(dto.getObservacao());
        cliente.setEmpresa(empresa);
        return cliente;
    }

    public ClienteResponseDTO toDto(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setId(cliente.getId());
        dto.setNome(cliente.getNome());
        dto.setEmail(cliente.getEmail());
        dto.setTelefone(cliente.getTelefone());
        dto.setCpf(cliente.getCpf());
        dto.setDataNascimento(cliente.getDataNascimento());
        dto.setPontosFidelidade(cliente.getPontosFidelidade());
        dto.setAtivo(cliente.getAtivo());
        dto.setObservacao(cliente.getObservacao());
        dto.setCreatedAt(cliente.getCreatedAt());
        return dto;
    }
}