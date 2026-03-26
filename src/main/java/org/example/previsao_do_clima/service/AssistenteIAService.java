package org.example.previsao_do_clima.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Service
public class AssistenteIAService {

    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public AssistenteIAService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String gerarRecomendacaoSeguranca(Double temp, Double umidade, String condicao) {
        String prompt = String.format("Atue como técnico de segurança do trabalho. Com base nos dados atuais (Temperatura: %s, Umidade: %s, Condição: %s), gere 3 recomendações curtas em português: 1. Melhor horário para trabalho externo; 2. Proteção/EPI necessário; 3. Vestimenta ideal.",
                temp, umidade, condicao);

        String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;

        Map<String, Object> requestBody = Map.of(
            "contents", java.util.List.of(
                Map.of("parts", java.util.List.of(
                    Map.of("text", prompt)
                ))
            )
        );

        try {
            Map<String, Object> response = webClient.post()
                    .uri(geminiUrl)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("candidates")) {
                java.util.List<Map<String, Object>> candidates = (java.util.List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    java.util.List<Map<String, Object>> parts = (java.util.List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return parts.get(0).get("text").toString().trim();
                    }
                }
            }
        } catch (Exception e) {
            return "1. Horário: Evite picos de sol. 2. EPI: Protetor solar e óculos. 3. Vestimenta: Roupas leves e claras.";
        }

        return "1. Horário: Evite picos de sol. 2. EPI: Protetor solar e óculos. 3. Vestimenta: Roupas leves e claras.";
    }
}