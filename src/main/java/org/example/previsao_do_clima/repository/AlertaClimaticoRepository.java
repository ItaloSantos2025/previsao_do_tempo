package org.example.previsao_do_clima.repository;

import org.example.previsao_do_clima.domain.entity.AlertaClimatico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertaClimaticoRepository extends JpaRepository<AlertaClimatico, String> {
    void deleteByCidadeId(String cidadeId); // Adicione isso
}