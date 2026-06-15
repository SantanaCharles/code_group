package com.codeGroup.repository;

import com.codeGroup.model.Projeto;
import com.codeGroup.model.enums.StatusProjeto;
import com.codeGroup.repository.projection.ResumoStatusProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ProjetoRepository extends JpaRepository<Projeto, Long>,
        JpaSpecificationExecutor<Projeto> {

    /**
     * Conta em quantos projetos "ativos" (status diferente dos informados) um
     * membro esta alocado. Usado para aplicar o limite de 3 alocacoes
     * simultaneas.
     */
    @Query("""
            select count(p) from Projeto p join p.membros m
            where m.id = :membroId and p.status not in :statusExcluidos
            """)
    long countProjetosAtivosDoMembro(@Param("membroId") Long membroId,
                                     @Param("statusExcluidos") Collection<StatusProjeto> statusExcluidos);

    /** Quantidade de projetos e total orcado agrupados por status. */
    @Query("""
            select p.status as status, count(p) as quantidade, sum(p.orcamentoTotal) as totalOrcado
            from Projeto p group by p.status
            """)
    List<ResumoStatusProjection> resumirPorStatus();

    /** Datas (inicio/real termino) dos projetos encerrados, para calculo de duracao media. */
    @Query("""
            select p.dataInicio as dataInicio, p.dataRealTermino as dataRealTermino
            from Projeto p
            where p.status = :status and p.dataRealTermino is not null
            """)
    List<DuracaoProjection> buscarDuracoesEncerrados(@Param("status") StatusProjeto status);

    /** Total de membros unicos alocados em qualquer projeto. */
    @Query("select count(distinct m.id) from Projeto p join p.membros m")
    long contarMembrosUnicosAlocados();

    interface DuracaoProjection {
        LocalDate getDataInicio();

        LocalDate getDataRealTermino();
    }
}
