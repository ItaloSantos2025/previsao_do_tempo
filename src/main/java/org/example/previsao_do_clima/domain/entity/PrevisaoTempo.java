package org.example.previsao_do_clima.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "previsao_tempo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrevisaoTempo {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne
    @JoinColumn(name = "cidade_id")
    private Cidade cidade;

    @Column(name = "data_hora_previsao")
    private LocalDateTime dataHoraPrevisao;

    private Double temperatura;
    private Double umidade;
    private String icone;

    @Column(name = "velocidade_vento")
    private Double velocidadeVento;

    @Column(name = "probabilidade_chuva")
    private Double probabilidadeChuva;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}