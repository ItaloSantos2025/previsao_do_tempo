package org.example.previsao_do_clima.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.previsao_do_clima.domain.entity.AlertaClimatico;
import org.example.previsao_do_clima.domain.entity.ClimaAtual;
import org.example.previsao_do_clima.domain.entity.PrevisaoTempo;
import org.example.previsao_do_clima.service.PrevisaoService;
import org.example.previsao_do_clima.repository.CidadeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clima")
@Tag(name = "Clima e Segurança do Trabalho", description = "API completa para o Dashboard OBJ SMART")
public class ClimaController {

    private final PrevisaoService previsaoService;
    private final CidadeRepository cidadeRepository;

    public ClimaController(PrevisaoService previsaoService, CidadeRepository cidadeRepository) {
        this.previsaoService = previsaoService;
        this.cidadeRepository = cidadeRepository;
    }

    @Operation(summary = "1. Obter Clima Atual, Ícone e Dicas IA", description = "Alimenta a temperatura principal, sensação, vento e a recomendação de segurança.")
    @GetMapping("/atual")
    public ResponseEntity<ClimaAtual> obterClimaAtual(
            @RequestParam(required = false) String cidade,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            String nomeBusca = identificarCidade(cidade, request);
            if (nomeBusca == null) return ResponseEntity.badRequest().build();

            ClimaAtual clima = previsaoService.buscarEProcessarClimaAtual(nomeBusca);
            gerenciarCookie(clima.getCidade().getId(), response);
            return ResponseEntity.ok(clima);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "2. Obter Previsão Próximas Horas/Dias", description = "Alimenta o gráfico horizontal (Imagem 1) e a lista diária (Imagem 3).")
    @GetMapping("/previsao-futura")
    public ResponseEntity<List<PrevisaoTempo>> obterPrevisaoFutura(
            @RequestParam(required = false) String cidade,
            HttpServletRequest request) {
        try {
            String nomeBusca = identificarCidade(cidade, request);
            if (nomeBusca == null) return ResponseEntity.badRequest().build();

            return ResponseEntity.ok(previsaoService.buscarPrevisaoFutura(nomeBusca));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "3. Obter Alertas Ativos (Card Amarelo)", description = "Retorna avisos de segurança. Se a lista vier vazia, esconda o card amarelo.")
    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaClimatico>> obterAlertas(
            @RequestParam(required = false) String cidade,
            HttpServletRequest request) {
        try {
            String nomeBusca = identificarCidade(cidade, request);
            if (nomeBusca == null) return ResponseEntity.badRequest().build();

            return ResponseEntity.ok(previsaoService.buscarAlertasAtivos(nomeBusca));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // --- Métodos Auxiliares (Mantidos Iguais) ---

    private String identificarCidade(String cidade, HttpServletRequest request) {
        if (cidade != null && !cidade.trim().isEmpty()) return cidade;

        if (request.getCookies() != null) {
            Optional<Cookie> cookie = Arrays.stream(request.getCookies())
                    .filter(c -> "OBJ_CITY_ID".equals(c.getName())).findFirst();
            if (cookie.isPresent()) {
                return cidadeRepository.findById(cookie.get().getValue())
                        .map(c -> c.getNome()).orElse(null);
            }
        }
        return null;
    }

    private void gerenciarCookie(String idCidade, HttpServletResponse response) {
        Cookie cookie = new Cookie("OBJ_CITY_ID", idCidade);
        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 dias
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}