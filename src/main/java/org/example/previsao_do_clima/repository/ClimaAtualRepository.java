package org.example.previsao_do_clima.repository;

import org.example.previsao_do_clima.domain.entity.ClimaAtual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClimaAtualRepository extends JpaRepository<ClimaAtual, String> {
    Optional<ClimaAtual> findByCidadeId(String cidadeId);
}
