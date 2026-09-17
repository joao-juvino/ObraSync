-- =============================================================================
-- V2__insert_initial_data.sql
-- ObraSync - Dados iniciais do sistema
-- =============================================================================
-- ATENCAO: A senha abaixo e o hash BCrypt de "admin123" (custo 10).
-- Troque esta senha imediatamente apos o primeiro login em producao.
-- Para gerar um novo hash: BCrypt.hashpw("nova_senha", BCrypt.gensalt(10))
-- =============================================================================

-- -------------------------------------------------------------------------
-- Usuario Administrador padrao
-- Login : admin@obrasync.com
-- Senha : admin123  (hash BCrypt abaixo)
-- -------------------------------------------------------------------------
INSERT INTO tb_usuario (nome, email, senha, perfil)
VALUES (
    'Administrador',
    'admin@obrasync.com',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoOe1s1KQH3Y4V5nM8rDtPFqX9bK2uJvGa',
    'ADMIN'
);

-- Engenheiro de exemplo para testes
INSERT INTO tb_usuario (nome, email, senha, perfil)
VALUES (
    'Eng. Joao Silva',
    'joao.silva@obrasync.com',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoOe1s1KQH3Y4V5nM8rDtPFqX9bK2uJvGa',
    'ENGENHEIRO'
);


-- -------------------------------------------------------------------------
-- Obras de teste
-- -------------------------------------------------------------------------
INSERT INTO tb_obra (nome, endereco, data_inicio, data_previsao_fim)
VALUES (
    'Edificio Residencial Aurora',
    'Rua das Palmeiras, 450 - Bairro Jardins, Sao Paulo - SP, 01452-000',
    '2024-03-01',
    '2026-12-31'
);

INSERT INTO tb_obra (nome, endereco, data_inicio, data_previsao_fim)
VALUES (
    'Complexo Comercial Horizonte',
    'Av. Brasil, 1200 - Centro, Campinas - SP, 13015-000',
    '2025-01-15',
    '2027-06-30'
);


-- -------------------------------------------------------------------------
-- Vistorias de exemplo vinculando obras e engenheiro
-- -------------------------------------------------------------------------

-- Vistoria aprovada na Obra 1
INSERT INTO tb_vistoria (obra_id, usuario_responsavel_id, tipo, data_vistoria, status, localizacao, observacoes)
VALUES (
    (SELECT id FROM tb_obra    WHERE nome = 'Edificio Residencial Aurora'),
    (SELECT id FROM tb_usuario WHERE email = 'joao.silva@obrasync.com'),
    'Estrutural',
    '2025-04-10 09:30:00',
    'APROVADA',
    'Pavimento 3 - Bloco A',
    'Estrutura de concreto dentro dos parametros de projeto. Nenhuma irregularidade encontrada.'
);

-- Vistoria pendente na Obra 2
INSERT INTO tb_vistoria (obra_id, usuario_responsavel_id, tipo, data_vistoria, status, localizacao, observacoes)
VALUES (
    (SELECT id FROM tb_obra    WHERE nome = 'Complexo Comercial Horizonte'),
    (SELECT id FROM tb_usuario WHERE email = 'joao.silva@obrasync.com'),
    'Instalacoes Eletricas',
    '2025-09-05 14:00:00',
    'PENDENTE',
    'Subsolo - Sala de Maquinas',
    'Aguardando laudo do eletricista responsavel para conclusao da vistoria.'
);
