package org.example.previsao_do_clima.service;

import org.example.previsao_do_clima.domain.entity.*;
import org.example.previsao_do_clima.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PrevisaoService {

    private final CidadeRepository cidadeRepository;
    private final ClimaAtualRepository climaAtualRepository;
    private final HistoricoClimaRepository historicoClimaRepository;
    private final PrevisaoTempoRepository previsaoTempoRepository;
    private final AlertaClimaticoRepository alertaClimaticoRepository;
    private final AssistenteIAService assistenteIAService;
    private final WebClient webClient;

    @Value("${openweathermap.api.key}")
    private String openWeatherMapApiKey;

    public PrevisaoService(CidadeRepository cidadeRepository,
                           ClimaAtualRepository climaAtualRepository,
                           HistoricoClimaRepository historicoClimaRepository,
                           PrevisaoTempoRepository previsaoTempoRepository,
                           AlertaClimaticoRepository alertaClimaticoRepository,
                           AssistenteIAService assistenteIAService,
                           WebClient.Builder webClientBuilder) {
        this.cidadeRepository = cidadeRepository;
        this.climaAtualRepository = climaAtualRepository;
        this.historicoClimaRepository = historicoClimaRepository;
        this.previsaoTempoRepository = previsaoTempoRepository;
        this.alertaClimaticoRepository = alertaClimaticoRepository;
        this.assistenteIAService = assistenteIAService;
        this.webClient = webClientBuilder.build();
    }

    @Transactional
    public ClimaAtual buscarEProcessarClimaAtual(String nomeCidade) {
        Cidade cidade = obterOuCriarCidade(nomeCidade);

        String owmUrl = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&lang=pt_br&appid=%s",
                nomeCidade, openWeatherMapApiKey);

        Map<String, Object> climaData = webClient.get().uri(owmUrl).retrieve().bodyToMono(Map.class).block();

        Map<String, Object> coord = (Map<String, Object>) climaData.get("coord");
        cidade.setLatitude(((Number) coord.get("lat")).doubleValue());
        cidade.setLongitude(((Number) coord.get("lon")).doubleValue());
        cidadeRepository.save(cidade);

        Map<String, Object> main = (Map<String, Object>) climaData.get("main");
        Map<String, Object> wind = (Map<String, Object>) climaData.get("wind");
        List<Map<String, Object>> weatherList = (List<Map<String, Object>>) climaData.get("weather");

        String desc = weatherList.get(0).get("description").toString();
        String icone = weatherList.get(0).get("icon").toString();

        Double temp = ((Number) main.get("temp")).doubleValue();
        Double umid = ((Number) main.get("humidity")).doubleValue();


        String recomendacao = assistenteIAService.gerarRecomendacaoSeguranca(temp, umid, desc);


        historicoClimaRepository.save(HistoricoClima.builder()
                .cidade(cidade).temperatura(temp).umidade(umid).precipitacao(0.0)
                .registradoAt(LocalDateTime.now()).build());


        ClimaAtual climaAtual = climaAtualRepository.findByCidadeId(cidade.getId())
                .orElse(new ClimaAtual());

        climaAtual.setCidade(cidade);
        climaAtual.setTemperatura(temp);
        climaAtual.setSensacaoTermica(((Number) main.get("feels_like")).doubleValue());
        climaAtual.setUmidade(umid);
        climaAtual.setPressao(((Number) main.get("pressure")).doubleValue());
        climaAtual.setVisibilidade(climaData.get("visibility") != null ? ((Number) climaData.get("visibility")).doubleValue() : null);
        climaAtual.setVelocidadeVento(((Number) wind.get("speed")).doubleValue());
        climaAtual.setDirecaoVento(wind.get("deg").toString());
        climaAtual.setCondicaoTempo(desc);
        climaAtual.setIcone(icone);
        climaAtual.setRecomendacaoIa(recomendacao);
        climaAtual.setColetadoAt(LocalDateTime.now());

        return climaAtualRepository.save(climaAtual);
    }

    @Transactional
    public List<PrevisaoTempo> buscarPrevisaoFutura(String nomeCidade) {
        Cidade cidade = obterOuCriarCidade(nomeCidade);

        String url = String.format("https://api.openweathermap.org/data/2.5/forecast?q=%s&units=metric&lang=pt_br&appid=%s",
                nomeCidade, openWeatherMapApiKey);

        Map<String, Object> response = webClient.get().uri(url).retrieve().bodyToMono(Map.class).block();
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");

        previsaoTempoRepository.deleteByCidadeId(cidade.getId());

        List<PrevisaoTempo> previsoes = new ArrayList<>();
        for (Map<String, Object> item : list) {
            Map<String, Object> main = (Map<String, Object>) item.get("main");
            Map<String, Object> wind = (Map<String, Object>) item.get("wind");
            List<Map<String, Object>> weather = (List<Map<String, Object>>) item.get("weather");

            PrevisaoTempo pt = PrevisaoTempo.builder()
                    .cidade(cidade)
                    .dataHoraPrevisao(LocalDateTime.ofInstant(Instant.ofEpochSecond(((Number) item.get("dt")).longValue()), ZoneId.systemDefault()))
                    .temperatura(((Number) main.get("temp")).doubleValue())
                    .umidade(((Number) main.get("humidity")).doubleValue())
                    .velocidadeVento(((Number) wind.get("speed")).doubleValue())
                    .probabilidadeChuva(item.containsKey("pop") ? ((Number) item.get("pop")).doubleValue() * 100 : 0.0)
                    .icone(weather.get(0).get("icon").toString())
                    .build();
            previsoes.add(pt);
        }
        return previsaoTempoRepository.saveAll(previsoes);
    }

    @Transactional
    public List<AlertaClimatico> buscarAlertasAtivos(String nomeCidade) {
        Cidade cidade = obterOuCriarCidade(nomeCidade);


        String urlOneCall = String.format("https://api.openweathermap.org/data/3.0/onecall?lat=%s&lon=%s&exclude=current,minutely,hourly,daily&appid=%s",
                cidade.getLatitude(), cidade.getLongitude(), openWeatherMapApiKey);

        try {
            Map<String, Object> response = webClient.get().uri(urlOneCall).retrieve().bodyToMono(Map.class).block();


            alertaClimaticoRepository.deleteByCidadeId(cidade.getId());

            if (response != null && response.containsKey("alerts")) {
                List<Map<String, Object>> alertsData = (List<Map<String, Object>>) response.get("alerts");
                List<AlertaClimatico> alertas = new ArrayList<>();

                for (Map<String, Object> alertItem : alertsData) {
                    AlertaClimatico ac = AlertaClimatico.builder()
                            .cidade(cidade)
                            .tipoAlerta(alertItem.get("event").toString())
                            .descricao(alertItem.get("description").toString())
                            .severidade("Moderada")
                            .inicioEm(LocalDateTime.ofInstant(Instant.ofEpochSecond(((Number) alertItem.get("start")).longValue()), ZoneId.systemDefault()))
                            .fimEm(LocalDateTime.ofInstant(Instant.ofEpochSecond(((Number) alertItem.get("end")).longValue()), ZoneId.systemDefault()))
                            .build();
                    alertas.add(ac);
                }
                return alertaClimaticoRepository.saveAll(alertas);
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }

    private Cidade obterOuCriarCidade(String nomeCidade) {
        Cidade cidade = cidadeRepository.findByNomeIgnoreCase(nomeCidade);
        if (cidade == null) {
            cidade = cidadeRepository.save(Cidade.builder().nome(nomeCidade).build());
        }
        return cidade;
    }
}