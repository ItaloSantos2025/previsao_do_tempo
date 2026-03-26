package org.example.previsao_do_clima.repository;

import org.example.previsao_do_clima.domain.entity.HistoricoClima;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoricoClimaRepository extends JpaRepository<HistoricoClima, String> {
}