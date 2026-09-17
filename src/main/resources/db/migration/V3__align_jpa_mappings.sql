-- Alinha os valores existentes ao armazenamento EnumType.STRING do JPA.
UPDATE tb_vistoria
SET tipo = CASE tipo
    WHEN 'Estrutural' THEN 'ESTRUTURAL'
    WHEN 'Instalacoes Eletricas' THEN 'ELETRICA'
    WHEN 'Instalações Elétricas' THEN 'ELETRICA'
    WHEN 'Hidraulica' THEN 'HIDRAULICA'
    WHEN 'Alvenaria' THEN 'ALVENARIA'
    WHEN 'Acabamento' THEN 'ACABAMENTO'
    WHEN 'Seguranca do Trabalho' THEN 'SEGURANCA_TRABALHO'
    ELSE tipo
END;

ALTER TABLE tb_vistoria
    ALTER COLUMN data_vistoria TYPE DATE USING data_vistoria::date;

ALTER TABLE tb_vistoria
    DROP CONSTRAINT IF EXISTS tb_vistoria_status_check;

ALTER TABLE tb_vistoria
    ADD CONSTRAINT tb_vistoria_status_check
    CHECK (status IN ('APROVADA', 'PENDENTE', 'REPROVADA', 'EM_ANDAMENTO'));
