package org.example.previsao_do_clima.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;
import java.util.List;

@Service
public class AssistenteIAService {

    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public AssistenteIAService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String gerarRecomendacaoSeguranca(Double temp, Double umidade, String condicao) {
        // Prompt sem caracteres especiais para teste

        String promptText = "Dados: " + temp + " graus, " + umidade + "% umidade. Gere 3 dicas curtas de seguranca do trabalho.";

        // Usando o modelo que seu terminal listou como o primeiro da lista (Estável 2.5)
        String geminiUrl = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;

        // JSON manual para não ter erro de hierarquia
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", promptText)
                        ))
                )
        );

        try {
            Map<String, Object> response = webClient.post()
                    .uri(geminiUrl)
                    .header("Content-Type", "application/json") // Forçando o header
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    return parts.get(0).get("text").toString().trim();
                }
            }
        } catch (Exception e) {
            // Se der erro, vamos printar a CAUSA real além da mensagem
            System.err.println("ERRO DETALHADO: " + e.getMessage());
            if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                String errorBody = ((org.springframework.web.reactive.function.client.WebClientResponseException) e).getResponseBodyAsString();
                System.err.println("CORPO DO ERRO DO GOOGLE: " + errorBody);
            }
            return "1. Horário: Evite picos de sol. 2. EPI: Protetor solar. 3. Vestimenta: Roupas leves.";
        }
        return "1. Horário: Evite picos de sol. 2. EPI: Protetor solar. 3. Vestimenta: Roupas leves.";
    }
}