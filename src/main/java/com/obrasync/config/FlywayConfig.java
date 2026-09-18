package com.obrasync.config;

import org.flywaydb.core.Flyway;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.sql.DataSource;
import java.util.logging.Logger;

/**
 * Bean EJB Singleton disparado na inicializacao do servidor (@Startup).
 *
 * Responsabilidades:
 *   1. Obter o DataSource JNDI configurado no WildFly (standalone.xml).
 *   2. Executar todas as migracoes Flyway pendentes em db/migration/.
 *
 * O Flyway detecta automaticamente os scripts V1__, V2__, etc. no classpath
 * (src/main/resources/db/migration) e aplica apenas os ainda nao executados,
 * registrando cada execucao na tabela de controle "flyway_schema_history".
 */
public class FlywayConfig {

    private static final Logger LOG = Logger.getLogger(FlywayConfig.class.getName());

    /** DataSource JNDI definido em standalone.xml / docker-compose.yml */
    @Resource(lookup = "java:jboss/datasources/PostgresDS")
    private DataSource dataSource;

    public void migrar() {
        LOG.info("[ObraSync] Iniciando migracoes Flyway...");
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    // Local dos scripts: src/main/resources/db/migration/
                    .locations("classpath:db/migration")
                    // Permite que o Flyway crie a tabela flyway_schema_history automaticamente
                    .createSchemas(true)
                    // Em caso de checksum incorreto (ex: edicao acidental de script ja aplicado),
                    // lancara excecao em vez de silenciar o erro
                    .validateOnMigrate(true)
                    .load();

            var resultado = flyway.migrate();
            LOG.info(String.format(
                "[ObraSync] Flyway concluiu: %d migracao(oes) aplicada(s). Versao atual: %s",
                resultado.migrationsExecuted,
                resultado.targetSchemaVersion
            ));
        } catch (Exception e) {
            // Lanca excecao para abortar o deploy em caso de falha de migracao,
            // evitando que o sistema rode com schema desatualizado.
            throw new RuntimeException("[ObraSync] ERRO CRITICO: Falha nas migracoes Flyway.", e);
        }
    }
}
