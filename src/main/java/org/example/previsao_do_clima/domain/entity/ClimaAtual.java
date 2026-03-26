package org.example.previsao_do_clima.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clima_atual")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClimaAtual {

    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @OneToOne
    @JoinColumn(name = "cidade_id", unique = true)
    private Cidade cidade;

    private Double temperatura;

    @Column(name = "sensacao_termica")
    private Double sensacaoTermica;

    private Double umidade;
    private Double pressao;
    private Double visibilidade;

    @Column(name = "velocidade_vento")
    private Double velocidadeVento;

    @Column(name = "direcao_vento")
    private String direcaoVento;

    @Column(name = "condicao_tempo")
    private String condicaoTempo;

    private String icone;

    @Column(name = "recomendacao_ia", columnDefinition = "TEXT")
    private String recomendacaoIa;

    @Column(name = "coletado_at", nullable = false)
    private LocalDateTime coletadoAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.coletadoAt == null) {
            this.coletadoAt = LocalDateTime.now();
        }
    }
}