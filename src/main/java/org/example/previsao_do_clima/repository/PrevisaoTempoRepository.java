package org.example.previsao_do_clima.repository;

import org.example.previsao_do_clima.domain.entity.PrevisaoTempo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrevisaoTempoRepository extends JpaRepository<PrevisaoTempo, String> {
    void deleteByCidadeId(String cidadeId); // Adicione isso
}