-- Migration V2: Tabela de auditoria para histórico de transações

CREATE TABLE payment_audit_log (
    id UUID NOT NULL,
    payment_id UUID NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_payment_audit_log PRIMARY KEY (id),
    CONSTRAINT fk_payment_audit_payments FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE
);

CREATE INDEX idx_payment_audit_payment_id ON payment_audit_log(payment_id)