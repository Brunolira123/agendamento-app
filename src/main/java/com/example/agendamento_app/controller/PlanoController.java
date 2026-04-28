package com.example.agendamento_app.controller;

import com.example.agendamento_app.model.Plano;
import com.example.agendamento_app.repository.PlanoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planos")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class PlanoController {

    private final PlanoRepository planoRepository;

    @GetMapping
    public ResponseEntity<List<Plano>> listarPlanos() {
        List<Plano> planos = planoRepository.findByAtivoTrueOrderByOrdemAsc();
        return ResponseEntity.ok(planos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Plano> buscarPorId(@PathVariable Long id) {
        return planoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Plano> buscarPorSlug(@PathVariable String slug) {
        return planoRepository.findBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}