// security/JwtAuthenticationFilter.java
package com.example.agendamento_app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// security/JwtAuthenticationFilter.java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String requestPath = request.getServletPath();

        System.out.println("🔄 Filtro executando para: " + requestPath);
        System.out.println("   Authorization header: " + (authHeader != null ? "Presente" : "Ausente"));

        // Pular validação para rotas de autenticação
        if (requestPath.contains("/api/auth/login") || requestPath.contains("/api/usuarios/login")) {
            System.out.println("⏭️ Pulando validação para rota pública");
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ Token não encontrado ou formato inválido");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token não fornecido\"}");
            return;
        }

        final String jwt = authHeader.substring(7);
        System.out.println("📝 Token recebido: " + jwt.substring(0, Math.min(50, jwt.length())) + "...");

        try {
            final String userEmail = jwtService.extractUsername(jwt);
            System.out.println("👤 Email extraído: " + userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("✅ Token validado com sucesso para: " + userEmail);
                } else {
                    System.out.println("❌ Token inválido para: " + userEmail);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Erro ao validar token: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token inválido: " + e.getMessage() + "\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

}