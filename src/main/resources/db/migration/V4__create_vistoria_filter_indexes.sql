-- Índices adicionais para filtros e ordenação usados pela listagem de vistorias.
CREATE INDEX IF NOT EXISTS idx_vistoria_tipo
    ON tb_vistoria (tipo);

CREATE INDEX IF NOT EXISTS idx_vistoria_data_vistoria
    ON tb_vistoria (data_vistoria DESC);

CREATE INDEX IF NOT EXISTS idx_vistoria_status_data
    ON tb_vistoria (status, data_vistoria DESC);
