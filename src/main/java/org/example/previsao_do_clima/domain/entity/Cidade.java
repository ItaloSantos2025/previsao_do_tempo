package org.example.previsao_do_clima.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "cidades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cidade {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    private String nome;
    private String estado;
    private String pais;
    private Double latitude;
    private Double longitude;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}