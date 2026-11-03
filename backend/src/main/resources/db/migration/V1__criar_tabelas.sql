-- Esquema inicial do TudoEmDia, conforme docs/06-modelo_de_dados.drawio e docs/05-regras_de_negocio.md

CREATE TABLE usuario (
    id_usuario              VARCHAR(36)  PRIMARY KEY,
    nome                    VARCHAR(255) NOT NULL,
    email                   VARCHAR(255) NOT NULL UNIQUE,
    senha                   VARCHAR(60)  NOT NULL,
    data_aceite_termos      TIMESTAMP    NOT NULL,
    tentativas_login_falhas INTEGER      NOT NULL DEFAULT 0,
    bloqueado_ate           TIMESTAMP,
    data_criacao            TIMESTAMP    NOT NULL
);

CREATE TABLE empresa (
    id_empresa   VARCHAR(36)  PRIMARY KEY,
    nome         VARCHAR(255) NOT NULL,
    cnpj         VARCHAR(14)  NOT NULL UNIQUE,
    data_criacao TIMESTAMP    NOT NULL
);

CREATE TABLE usuario_empresa (
    id_usuario_empresa VARCHAR(36) PRIMARY KEY,
    id_usuario         VARCHAR(36) NOT NULL REFERENCES usuario (id_usuario),
    id_empresa         VARCHAR(36) NOT NULL REFERENCES empresa (id_empresa),
    perfil             VARCHAR(20) NOT NULL CHECK (perfil IN ('PROPRIETARIO', 'FINANCEIRO', 'CONTADOR')),
    ativo              BOOLEAN     NOT NULL DEFAULT TRUE,
    recebe_alertas     BOOLEAN     NOT NULL DEFAULT TRUE,
    UNIQUE (id_usuario, id_empresa)
);

CREATE INDEX idx_usuario_empresa_empresa ON usuario_empresa (id_empresa);

CREATE TABLE convite (
    id_convite            VARCHAR(36)  PRIMARY KEY,
    id_empresa            VARCHAR(36)  NOT NULL REFERENCES empresa (id_empresa),
    id_usuario_convidante VARCHAR(36)  NOT NULL REFERENCES usuario (id_usuario),
    email_convidado       VARCHAR(255) NOT NULL,
    -- Apenas financeiro ou contador podem ser convidados (RN14)
    perfil_convidado      VARCHAR(20)  NOT NULL CHECK (perfil_convidado IN ('FINANCEIRO', 'CONTADOR')),
    status                VARCHAR(20)  NOT NULL CHECK (status IN ('PENDENTE', 'ACEITO', 'EXPIRADO', 'CANCELADO')),
    data_envio            TIMESTAMP    NOT NULL,
    token_hash            VARCHAR(255) NOT NULL UNIQUE,
    data_expiracao        TIMESTAMP    NOT NULL
);

CREATE INDEX idx_convite_empresa ON convite (id_empresa);

-- id_empresa nulo indica categoria padrão do sistema (RN41)
CREATE TABLE categoria (
    id_categoria VARCHAR(36) PRIMARY KEY,
    id_empresa   VARCHAR(36) REFERENCES empresa (id_empresa),
    nome         TEXT        NOT NULL
);

CREATE INDEX idx_categoria_empresa ON categoria (id_empresa);

CREATE TABLE recorrencia (
    id_recorrencia VARCHAR(36)    PRIMARY KEY,
    id_empresa     VARCHAR(36)    NOT NULL REFERENCES empresa (id_empresa),
    id_categoria   VARCHAR(36)    NOT NULL REFERENCES categoria (id_categoria),
    descricao      TEXT           NOT NULL,
    valor          NUMERIC(10, 2) NOT NULL CHECK (valor > 0),
    periodicidade  VARCHAR(10)    NOT NULL CHECK (periodicidade IN ('MENSAL', 'ANUAL')),
    data_inicio    DATE           NOT NULL,
    data_fim       DATE,
    ativa          BOOLEAN        NOT NULL DEFAULT TRUE,
    CHECK (data_fim IS NULL OR data_fim >= data_inicio)
);

CREATE INDEX idx_recorrencia_empresa ON recorrencia (id_empresa);

CREATE TABLE obrigacao (
    id_obrigacao           VARCHAR(36)    PRIMARY KEY,
    id_empresa             VARCHAR(36)    NOT NULL REFERENCES empresa (id_empresa),
    id_categoria           VARCHAR(36)    NOT NULL REFERENCES categoria (id_categoria),
    id_recorrencia         VARCHAR(36)    REFERENCES recorrencia (id_recorrencia),
    descricao              TEXT           NOT NULL,
    -- Valor em reais, maior que zero, com duas casas decimais (RN27)
    valor                  NUMERIC(10, 2) NOT NULL CHECK (valor > 0),
    data_vencimento        DATE           NOT NULL,
    status                 VARCHAR(20)    NOT NULL CHECK (status IN ('PENDENTE', 'PAGA', 'VENCIDA')),
    data_criacao           TIMESTAMP      NOT NULL,
    id_usuario_atualizacao VARCHAR(36)    REFERENCES usuario (id_usuario),
    data_atualizacao       TIMESTAMP,
    -- Uma única ocorrência por recorrência e data (RN64)
    UNIQUE (id_recorrencia, data_vencimento)
);

CREATE INDEX idx_obrigacao_empresa_vencimento ON obrigacao (id_empresa, data_vencimento);
CREATE INDEX idx_obrigacao_categoria ON obrigacao (id_categoria);
CREATE INDEX idx_obrigacao_status_vencimento ON obrigacao (status, data_vencimento);

CREATE TABLE anexo_obrigacao (
    id_anexo        VARCHAR(36) PRIMARY KEY,
    id_obrigacao    VARCHAR(36) NOT NULL REFERENCES obrigacao (id_obrigacao),
    nome_arquivo    TEXT        NOT NULL,
    caminho_arquivo TEXT        NOT NULL,
    data_envio      TIMESTAMP   NOT NULL
);

CREATE INDEX idx_anexo_obrigacao_obrigacao ON anexo_obrigacao (id_obrigacao);

CREATE TABLE documento (
    id_documento           VARCHAR(36) PRIMARY KEY,
    id_empresa             VARCHAR(36) NOT NULL REFERENCES empresa (id_empresa),
    id_categoria           VARCHAR(36) NOT NULL REFERENCES categoria (id_categoria),
    descricao              TEXT        NOT NULL,
    data_validade          DATE,
    nome_arquivo           TEXT        NOT NULL,
    caminho_arquivo        TEXT        NOT NULL,
    status                 VARCHAR(20) NOT NULL CHECK (status IN ('ATIVO', 'VENCIDO', 'ARQUIVADO')),
    data_criacao           TIMESTAMP   NOT NULL,
    id_usuario_atualizacao VARCHAR(36) REFERENCES usuario (id_usuario),
    data_atualizacao       TIMESTAMP
);

CREATE INDEX idx_documento_empresa ON documento (id_empresa);
CREATE INDEX idx_documento_categoria ON documento (id_categoria);

CREATE TABLE pagamento (
    id_pagamento            VARCHAR(36)    PRIMARY KEY,
    id_obrigacao            VARCHAR(36)    NOT NULL REFERENCES obrigacao (id_obrigacao),
    id_usuario              VARCHAR(36)    NOT NULL REFERENCES usuario (id_usuario),
    valor_pago              NUMERIC(10, 2) NOT NULL CHECK (valor_pago > 0),
    data_pagamento          DATE           NOT NULL,
    data_registro           TIMESTAMP      NOT NULL,
    cancelado               BOOLEAN        NOT NULL DEFAULT FALSE,
    id_usuario_cancelamento VARCHAR(36)    REFERENCES usuario (id_usuario),
    data_cancelamento       TIMESTAMP
);

-- No máximo um pagamento vigente por obrigação (RN69)
CREATE UNIQUE INDEX uk_pagamento_obrigacao_vigente ON pagamento (id_obrigacao) WHERE cancelado = FALSE;

CREATE TABLE configuracao_alerta (
    id_configuracao   VARCHAR(36) PRIMARY KEY,
    id_empresa        VARCHAR(36) NOT NULL REFERENCES empresa (id_empresa),
    dias_antecedencia INTEGER     NOT NULL CHECK (dias_antecedencia >= 0),
    ativo             BOOLEAN     NOT NULL DEFAULT TRUE,
    UNIQUE (id_empresa, dias_antecedencia)
);

CREATE TABLE alerta_enviado (
    id_alerta         VARCHAR(36) PRIMARY KEY,
    id_obrigacao      VARCHAR(36) NOT NULL REFERENCES obrigacao (id_obrigacao),
    id_usuario        VARCHAR(36) NOT NULL REFERENCES usuario (id_usuario),
    dias_antecedencia INTEGER     NOT NULL CHECK (dias_antecedencia >= 0),
    data_envio        TIMESTAMP   NOT NULL,
    -- Um único envio por obrigação, destinatário e prazo (RN78)
    UNIQUE (id_obrigacao, id_usuario, dias_antecedencia)
);

-- Categorias padrão do sistema (RN41)
INSERT INTO categoria (id_categoria, id_empresa, nome) VALUES
    (gen_random_uuid()::TEXT, NULL, 'Impostos'),
    (gen_random_uuid()::TEXT, NULL, 'Fornecedores'),
    (gen_random_uuid()::TEXT, NULL, 'Folha de pagamento'),
    (gen_random_uuid()::TEXT, NULL, 'Aluguel e consumo'),
    (gen_random_uuid()::TEXT, NULL, 'Outros');
