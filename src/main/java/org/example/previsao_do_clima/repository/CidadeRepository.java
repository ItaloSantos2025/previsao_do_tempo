package org.example.previsao_do_clima.repository;

import org.example.previsao_do_clima.domain.entity.Cidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CidadeRepository extends JpaRepository<Cidade, String> {
    Cidade findByNomeIgnoreCase(String nome);
}