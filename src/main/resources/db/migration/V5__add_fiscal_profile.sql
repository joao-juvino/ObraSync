ALTER TABLE tb_usuario DROP CONSTRAINT IF EXISTS tb_usuario_perfil_check;
ALTER TABLE tb_usuario ADD CONSTRAINT tb_usuario_perfil_check
    CHECK (perfil IN ('ADMIN', 'ENGENHEIRO', 'FISCAL'));
