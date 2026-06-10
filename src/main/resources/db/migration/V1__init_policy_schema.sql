-- Baseline schema for the Policy Overview BFF (APAC-2847).
-- Enum-typed columns store the enum constant name (e.g. ACTIVE, ACCIDENT_AND_HEALTH,
-- HONG_KONG); display transformation happens in the application layer.
CREATE TABLE policy (
    id                  UUID            NOT NULL,
    policy_number       VARCHAR(64)     NOT NULL,
    policyholder_name   VARCHAR(255)    NOT NULL,
    line_of_business    VARCHAR(32)     NOT NULL,
    status              VARCHAR(32)     NOT NULL,
    premium_amount      NUMERIC(15, 2)  NOT NULL,
    currency            VARCHAR(3)      NOT NULL,
    effective_date      DATE            NOT NULL,
    expiry_date         DATE            NOT NULL,
    region              VARCHAR(32)     NOT NULL,
    underwriter         VARCHAR(255)    NOT NULL,
    flagged_for_review  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ     NOT NULL,
    updated_at          TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_policy PRIMARY KEY (id),
    CONSTRAINT uq_policy_policy_number UNIQUE (policy_number)
);

-- Indexes supporting the list endpoint's filtering and search.
-- policy_number is already indexed via the uq_policy_policy_number unique constraint.
CREATE INDEX idx_policy_status ON policy (status);
CREATE INDEX idx_policy_line_of_business ON policy (line_of_business);
CREATE INDEX idx_policy_region ON policy (region);
CREATE INDEX idx_policy_effective_date ON policy (effective_date);
