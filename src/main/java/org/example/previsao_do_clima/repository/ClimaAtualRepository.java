package org.example.previsao_do_clima.repository;

import org.example.previsao_do_clima.domain.entity.ClimaAtual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClimaAtualRepository extends JpaRepository<ClimaAtual, String> {

    /**
     * Busca o registro de clima atual vinculado a uma cidade específica.
     * Útil para recuperar os dados quando o usuário já tem o ID da cidade no Cookie.
     */
    Optional<ClimaAtual> findByCidadeId(String cidadeId);

    /**
     * Deleta o registro de clima atual de uma cidade.
     * Útil em processos de limpeza ou atualização forçada.
     */
    void deleteByCidadeId(String cidadeId);
}