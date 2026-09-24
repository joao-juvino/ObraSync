# ObraSync 1.0 Finalization

## Estado inicial

Auditoria de 23/09/2026, checkout inicialmente limpo. Stack Java 11 / Jakarta EE 8 preservada. Foram inspecionados fontes, testes, migrations, telas, relatório, build e infraestrutura versionados.

## Problemas encontrados

- Dockerfile já configurava o datasource; README afirmava incorretamente configuração manual. Compose dependia de WAR local.
- Flyway executado pelo container; `FlywayConfig` era código morto (sem Startup/PostConstruct), com dependência redundante no WAR.
- JPA estava com `hbm2ddl.auto=none`, apesar da documentação afirmar validação.
- Segredo JWT e credenciais demonstrativas de banco estavam versionados. Devem ser considerados públicos; qualquer uso externo exige troca. Histórico Git não foi reescrito.
  O segredo JWT antigo aparece no commit `9d40a64`. Não há confirmação de uso como segredo real fora do projeto; se tiver sido reutilizado, é necessária sua rotação nesse ambiente.
- Filtro JWT expunha detalhes de exceção e seu `isSecure()` recursava sobre o próprio contexto.
- Ações JSF não verificavam perfis no servidor. Atualização de vistoria por merge podia substituir a coleção de evidências.
- Evidências possuíam somente entidade e escrita local, sem endpoints, UI, exclusão ou validação do conteúdo.
- README anunciava PATCH inexistente e indicava caminho errado do template Jasper.
- JaCoCo sem check; CI executava testes duas vezes. Nenhum target/log/.env estava rastreado pelo Git.

## Alterações realizadas

- Versão Maven 1.0.0, compilação com `--release 11`, plugins de build/teste fixados e JaCoCo Check de 70% de linhas.
- Docker multi-stage constrói/testa o WAR e copia o driver resolvido pelo Maven. Removida dependência de WAR no host; datasource por CLI com expressões de ambiente; perfil MicroProfile habilitado.
- Compose com healthcheck PostgreSQL, volumes `pgdata`/`evidencias`, Flyway como único migrador e dependência de conclusão antes do WildFly. Somente 8080 publicado em localhost.
- `.env.example`, exclusão de `.env` do Git e do contexto Docker. Nenhum `.env` real foi criado ou sobrescrito.
- Removidos `FlywayConfig.java` (código morto) e o JAR PostgreSQL versionado e agora redundante. Recuperáveis pelo histórico Git; driver obtido pelo build Maven. Nenhum teste foi removido.
- JPA com validação de schema. Migration V8 alinha a coluna de senha e adiciona conta FISCAL demonstrativa, sem editar V1–V7.
- JWT externalizado e validado na inicialização; contexto de segurança corrigido; erros públicos padronizados; perfis consultados no banco para REST.
- `AccessPolicy` compartilhada nos services: ADMIN administra; ENGENHEIRO edita somente vistorias próprias; FISCAL consulta e altera status por operação específica. Login JSF troca ID de sessão.
- Evidências: MIME e conteúdo real JPEG/PNG, máximo 10 MB/20 megapixels, UUID, raiz configurável, proteção de caminho, DTO, quatro endpoints e galeria/upload/remoção JSF.
- Compensação de arquivos vinculada ao resultado JTA: remove uploads em rollback, restaura exclusões em rollback e limpa arquivos após commit. Exclusão de vistoria inclui os arquivos relacionados.
- Atualização de vistoria preserva sua coleção de evidências; associações inexistentes, paginação com overflow e filtros de data inválidos são rejeitados.
- JasperReports anexa imagens e tolera arquivos ausentes/inválidos; OpenPDF resolvido pelo Maven Central, sem o artefato customizado do repositório legado Jaspersoft.
- OpenAPI completo com schemas, respostas, filtros, paginação, Bearer e binários; teste compara métodos/caminhos com os recursos reais. Swagger UI aponta para `/openapi`.
- Polimento pontual de formulários, mensagens, permissões visuais e galeria, preservando a interface.
- CI usa Java 11, cache, `mvn clean verify`, verificação de WAR e Docker build. Sem publicação/deploy automáticos.
- README refeito para a implementação real; screenshots documentados, sem imagens inventadas.

### Validação executada

- `mvn clean verify --batch-mode`: **BUILD SUCCESS**, em 23/09/2026.
- **71 testes**, zero falhas, zero erros, zero ignorados.
- **81,97% de cobertura LINE**: 582 linhas cobertas de 710 no escopo definido; mínimo 70% aprovado.
- JaCoCo: `target/site/jacoco/index.html`. Excluídos modelos/DTOs, resultado de paginação e configuração declarativa; regras, segurança, services, REST, controllers e relatórios permanecem incluídos.
- WAR gerado em `target/obrasync.war`. XML, XHTML e JRXML verificados como XML bem formado; `git diff --check` sem erros de whitespace.
- Java disponível neste host: 17. Compilação usa API/alvo Java 11; execução efetiva sob Java 11 está configurada no CI e no estágio Maven do Docker, mas não foi executada neste host.
- `docker compose build`: não executou porque `docker` não está instalado/disponível no PATH. O executável também não foi encontrado no caminho padrão do Docker Desktop. Sem Docker, não foi possível subir o Compose nem verificar deployment, banco, datasource ou interface no browser.
- Não foram realizados commit, push, tag, reescrita de histórico ou exclusão de volumes/dados.

## Checklist v1.0

Itens de lógica marcados abaixo foram verificados por testes unitários; não equivalem a teste integrado em WildFly. Itens de infraestrutura que exigem execução permanecem abertos.

- [x] clean build funciona — Maven local
- [x] mvn clean verify passa
- [x] cobertura >= 70%
- [x] GitHub Actions correto — configuração revisada; execução remota não disparada
- [ ] Docker build funciona — ambiente sem Docker
- [ ] Docker Compose reproduzível — configuração pronta, execução pendente
- [x] PostgreSQL possui healthcheck — configurado no Compose
- [ ] Flyway funciona — migrations aguardam execução no PostgreSQL
- [ ] datasource automático — script preparado, validação no WildFly pendente
- [ ] WAR automático — WAR Maven validado; deploy no container pendente
- [x] login funciona — BCrypt e sessão verificados em testes
- [x] JWT funciona — assinatura, expiração e configuração verificadas
- [x] RBAC funciona — política testada com os três perfis
- [x] CRUD de vistorias funciona — resources/services testados com JPA simulado
- [x] filtros funcionam — JPQL parametrizado e entradas inválidas testados
- [x] paginação funciona — limites/offset e overflow testados
- [x] evidências funcionam — storage real temporário, API, bean e compensação testados
- [x] JasperReports funciona — PDFs reais gerados, com/sem evidências e imagem ausente
- [ ] Swagger funciona — assets e rota configurados, browser pendente
- [ ] OpenAPI funciona — contrato conferido por teste; endpoint no WildFly pendente
- [x] secrets externalizados
- [x] README atualizado
- [x] arquitetura documentada
- [x] Quick Start documentado
- [x] screenshots preparados/documentados
- [x] nenhum TODO crítico

## Limitações conhecidas

- Docker não localizado neste ambiente, inclusive na retomada de 24/09/2026. Java local disponível: 17; alvo da aplicação: 11.
- O storage é local e de instância única. A compensação cobre rollback normal, mas uma interrupção abrupta do processo entre banco e filesystem pode exigir reconciliação de arquivos `.deleted`. Não há transação distribuída entre os dois recursos.
- PrimeFlex e Swagger UI carregam assets de CDN. As contas de seed são públicas e exclusivamente demonstrativas.
- A tabela JSF pagina a lista carregada; filtros e paginação REST são executados no banco.
- Screenshots não foram fabricados. Os cinco estados a capturar estão em `docs/screenshots/README.md`.

## Status

**NOT READY** — implementação e validação Maven concluídas; falta comprovar a execução integrada em Docker/Java 11.

Para encerrar a validação em um host com Docker, executar o Quick Start do README, confirmar migrations, datasource e deploy nos logs, e exercitar login, permissões, CRUD, filtros, paginação, upload/exclusão de fotos, laudo e Swagger/OpenAPI. A ausência de Docker impediu essa etapa neste host; não foi tratada como sucesso.

As capturas reais são o único artefato visual restante. Não foram propostas funcionalidades além do escopo da v1.0.
