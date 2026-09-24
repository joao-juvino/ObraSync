-- Mantém migrations aplicadas intactas e alinha tipos com JPA.
ALTER TABLE tb_usuario ALTER COLUMN senha TYPE VARCHAR(255);
INSERT INTO tb_usuario (nome, email, senha, perfil)
SELECT 'Fiscal de demonstração', 'fiscal@obrasync.com', senha, 'FISCAL'
FROM tb_usuario WHERE email = 'admin@obrasync.com';
