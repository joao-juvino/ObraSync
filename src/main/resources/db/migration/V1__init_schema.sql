-- =============================================================================
-- V1__init_schema.sql
-- ObraSync - Criacao inicial do schema do banco de dados
-- Banco: PostgreSQL 12+
-- Flyway versao: 9.x
-- =============================================================================

-- -------------------------------------------------------------------------
-- Tabela: tb_usuario
-- Armazena os usuarios do sistema (administradores e engenheiros).
-- -------------------------------------------------------------------------
CREATE TABLE tb_usuario (
    id      BIGSERIAL       PRIMARY KEY,
    nome    VARCHAR(150)    NOT NULL,
    email   VARCHAR(255)    NOT NULL UNIQUE,
    -- Senha armazenada como hash BCrypt ($2a$...), tamanho fixo de 60 chars
    senha   CHAR(60)        NOT NULL,
    -- Perfil de acesso: valor textual do enum Java (ADMIN | ENGENHEIRO)
    perfil  VARCHAR(20)     NOT NULL
                            CHECK (perfil IN ('ADMIN', 'ENGENHEIRO'))
);

COMMENT ON TABLE  tb_usuario           IS 'Usuarios do sistema ObraSync';
COMMENT ON COLUMN tb_usuario.senha     IS 'Hash BCrypt da senha (60 chars)';
COMMENT ON COLUMN tb_usuario.perfil    IS 'Perfil de acesso: ADMIN ou ENGENHEIRO';


-- -------------------------------------------------------------------------
-- Tabela: tb_obra
-- Representa uma obra / empreendimento gerenciado pelo sistema.
-- -------------------------------------------------------------------------
CREATE TABLE tb_obra (
    id                  BIGSERIAL       PRIMARY KEY,
    nome                VARCHAR(200)    NOT NULL,
    endereco            VARCHAR(500)    NOT NULL,
    data_inicio         DATE            NOT NULL,
    data_previsao_fim   DATE            NOT NULL,

    CONSTRAINT chk_obra_datas
        CHECK (data_previsao_fim >= data_inicio)
);

COMMENT ON TABLE  tb_obra                    IS 'Obras e empreendimentos cadastrados';
COMMENT ON COLUMN tb_obra.data_previsao_fim  IS 'Deve ser igual ou posterior a data de inicio';


-- -------------------------------------------------------------------------
-- Tabela: tb_vistoria
-- Registra as vistorias realizadas em uma obra por um usuario responsavel.
-- -------------------------------------------------------------------------
CREATE TABLE tb_vistoria (
    id              BIGSERIAL       PRIMARY KEY,

    -- Relacionamento com a obra vistoriada
    obra_id         BIGINT          NOT NULL
                                    REFERENCES tb_obra(id)
                                    ON DELETE CASCADE,

    -- Usuario responsavel pela vistoria
    usuario_responsavel_id BIGINT   NOT NULL
                                    REFERENCES tb_usuario(id)
                                    ON DELETE RESTRICT,

    tipo            VARCHAR(100)    NOT NULL,
    data_vistoria   TIMESTAMP       NOT NULL DEFAULT NOW(),

    -- Status textual do enum Java: APROVADA | PENDENTE | REPROVADA
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDENTE'
                                    CHECK (status IN ('APROVADA', 'PENDENTE', 'REPROVADA')),

    localizacao     VARCHAR(300),
    observacoes     TEXT
);

COMMENT ON TABLE  tb_vistoria                        IS 'Vistorias realizadas nas obras';
COMMENT ON COLUMN tb_vistoria.obra_id                IS 'FK para tb_obra; cascade delete';
COMMENT ON COLUMN tb_vistoria.usuario_responsavel_id IS 'FK para tb_usuario; restrict delete';
COMMENT ON COLUMN tb_vistoria.status                 IS 'APROVADA | PENDENTE | REPROVADA';


-- -------------------------------------------------------------------------
-- Indices para otimizar as consultas mais comuns
-- -------------------------------------------------------------------------
CREATE INDEX idx_vistoria_obra_id     ON tb_vistoria(obra_id);
CREATE INDEX idx_vistoria_usuario_id  ON tb_vistoria(usuario_responsavel_id);
CREATE INDEX idx_vistoria_status      ON tb_vistoria(status);
CREATE INDEX idx_usuario_email        ON tb_usuario(email);
