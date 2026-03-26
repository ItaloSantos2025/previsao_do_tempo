package org.example.previsao_do_clima.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historico_clima")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoClima {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne
    @JoinColumn(name = "cidade_id")
    private Cidade cidade;

    private Double temperatura;
    private Double umidade;
    private Double precipitacao;
    
    @Column(name = "registrado_at")
    private LocalDateTime registradoAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}