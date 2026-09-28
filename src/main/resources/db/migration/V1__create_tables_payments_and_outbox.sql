-- Migration V1: Criação das tabelas fundamentais de pagamentos e transactional outbox

CREATE TABLE payments (
    id UUID NOT NULL,
    customer_id VARCHAR(255) NOT NULL,
    amount NUMERIC(38, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_payments PRIMARY KEY (id)
);

CREATE TABLE payments_outbox (
    id UUID NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_payments_outbox PRIMARY KEY (id)
);

-- Ìndices para otimização de consultas e do worker do Outbox Pattern
CREATE INDEX idx_payments_customer_id ON payments(customer_id);
CREATE INDEX idx_payments_outbox_processed_created ON payments_outbox(processed, created_at);

