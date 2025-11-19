package com.huntercodexs.integration.retry;
//DEV
import com.huntercodexs.persisteposvenda.adapter.database.entity.TabelaMongoDbEntity;
import com.huntercodexs.persisteposvenda.adapter.database.repository.BasePosVendaRepository;
import com.huntercodexs.persisteposvenda.api.model.CnsgPosVendaAtualizacaoRequisicao;
import com.huntercodexs.persisteposvenda.api.model.CnsgPosVendaContratoPorIdResposta;
import com.huntercodexs.persisteposvenda.api.model.CnsgPosVendaIncluirRegistroRequisicao;
import com.huntercodexs.persisteposvenda.api.model.CnsgPosVendaIncluirRegistroResposta;
import com.huntercodexs.persisteposvenda.application.config.mongo.RetryTemplateMongoConfig;
import com.huntercodexs.persisteposvenda.application.domain.exception.PosVendaException;
import com.huntercodexs.persisteposvenda.application.usecase.BasePosVendaUseCase;
import lombok.RequiredArgsConstructor;
import org.codehaus.commons.nullanalysis.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Sample implements BasePosVendaUseCase {

    private static final Logger log = LoggerFactory.getLogger(Sample.class);

    private final BasePosVendaRepository basePosVendaRepository;
    private final RetryTemplateMongoConfig retryTemplateMongoConfig;

    @Override
    public CnsgPosVendaIncluirRegistroResposta incluirRegistro(final CnsgPosVendaIncluirRegistroRequisicao reqIncluirRegistro) {

        TabelaMongoDbEntity entidade = new TabelaMongoDbEntity(reqIncluirRegistro);

        log.info("Incluindo registro na base de dados: {}", entidade.getId());

        try {

            String id = retryTemplateMongoConfig.mongoRetry().execute(context -> {
                log.info("MongoDB Tentativa para inclusao #{}", context.getRetryCount()+1);
                return basePosVendaRepository.save(entidade).getId();
            });

            log.info("Registro incluído com sucesso na base de dados");
            return new CnsgPosVendaIncluirRegistroResposta()
                    .id(id)
                    .message("Registro incluído com sucesso na base de dados");

        } catch (Exception e) {

            // Erro de chave duplicada no MongoDB
            if (e.getMessage().contains("E11000")) {
                log.error("Erro de chave duplicada ao incluir registro na base de dados: {}", e.getMessage());
                throw new PosVendaException("4001100");
            }

            log.error("Erro ao incluir registro na base de dados: {}", e.getMessage());
            throw new PosVendaException("5001143");

        }
    }

    @Override
    public CnsgPosVendaContratoPorIdResposta consultarRegistro(@NotNull final String numeroContrato) {
        log.info("Consultando registro na base de dados para o contrato: {}", numeroContrato);

        var entidadeOpcional = basePosVendaRepository.findByNumeroContrato(numeroContrato);

        log.info("Consulta finalizada: {}", entidadeOpcional);

        if (entidadeOpcional.isPresent()) {
            return new CnsgPosVendaContratoPorIdResposta()
                    .id(entidadeOpcional.get().getId())
                    .situacaoRegistro(entidadeOpcional.get().getSituacaoRegistro());
        } else {
            throw new PosVendaException("4040312");
        }
    }

    @Override
    public void atualizarRegistro(final String numeroContrato, CnsgPosVendaAtualizacaoRequisicao reqAtualizarRegistro) {

        log.info("Buscando registro na base de dados para atualização: {}", reqAtualizarRegistro);

        var entidadeOpcional = basePosVendaRepository.findByNumeroContrato(numeroContrato);

        log.info("Busca finalizada: {}", entidadeOpcional);

        if (entidadeOpcional.isPresent()) {

            var entidade = entidadeOpcional.get();

            entidade.setSituacaoRegistro(reqAtualizarRegistro.getSituacaoRegistro());

            log.info("Atualizando registro na base de dados: {}", entidade);

            retryTemplateMongoConfig.mongoRetry().execute(context -> {
                log.info("MongoDB Tentativa para atualizacao #{}", context.getRetryCount()+1);
                return basePosVendaRepository.save(entidade);
            });

            log.info("Registro atualizado com sucesso na base de dados");

        } else {
            throw new PosVendaException("4040312");
        }
    }

}
