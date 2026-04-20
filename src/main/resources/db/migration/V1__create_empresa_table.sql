-- Tabela empresa
CREATE TABLE IF NOT EXISTS empresa (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    slug VARCHAR(50) UNIQUE NOT NULL,
    nicho VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    telefone VARCHAR(20),
    config JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela profissional
CREATE TABLE IF NOT EXISTS profissional (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    telefone VARCHAR(20),
    ativo BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela servico
CREATE TABLE IF NOT EXISTS servico (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10,2) NOT NULL,
    duracao_minutos INT NOT NULL,
    ativo BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela agendamento
CREATE TABLE IF NOT EXISTS agendamento (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
    profissional_id BIGINT NOT NULL REFERENCES profissional(id),
    servico_id BIGINT NOT NULL REFERENCES servico(id),
    cliente_nome VARCHAR(100) NOT NULL,
    cliente_telefone VARCHAR(20),
    data_hora TIMESTAMP NOT NULL,
    duracao_minutos INT NOT NULL,
    status VARCHAR(20) DEFAULT 'AGENDADO',
    preco_cobrado DECIMAL(10,2),
    observacao TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices
CREATE INDEX idx_agendamento_empresa_data ON agendamento(empresa_id, data_hora);
CREATE INDEX idx_agendamento_profissional_data ON agendamento(profissional_id, data_hora);
CREATE INDEX idx_agendamento_status ON agendamento(status);
CREATE INDEX idx_profissional_empresa ON profissional(empresa_id);
CREATE INDEX idx_servico_empresa ON servico(empresa_id);