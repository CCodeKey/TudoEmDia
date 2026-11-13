-- O registro de alerta enviado não tem valor sem a obrigação: ao excluí-la, o banco remove os registros juntos.
-- Evita que um alerta gravado em segundo plano, durante a exclusão, impeça a obrigação de ser excluída.
ALTER TABLE alerta_enviado DROP CONSTRAINT alerta_enviado_id_obrigacao_fkey;

ALTER TABLE alerta_enviado
    ADD CONSTRAINT alerta_enviado_id_obrigacao_fkey
    FOREIGN KEY (id_obrigacao) REFERENCES obrigacao (id_obrigacao) ON DELETE CASCADE;
