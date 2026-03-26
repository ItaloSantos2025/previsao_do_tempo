package org.example.previsao_do_clima.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alertas_climaticos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaClimatico {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne
    @JoinColumn(name = "cidade_id")
    private Cidade cidade;

    @Column(name = "tipo_alerta")
    private String tipoAlerta;
    
    private String descricao;
    private String severidade;
    
    @Column(name = "inicio_em")
    private LocalDateTime inicioEm;
    
    @Column(name = "fim_em")
    private LocalDateTime fimEm;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}