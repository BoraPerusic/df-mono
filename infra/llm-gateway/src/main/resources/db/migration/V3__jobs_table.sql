CREATE TABLE jobs (
    id VARCHAR(255) PRIMARY KEY,
    status VARCHAR(50) NOT NULL, -- QUEUED, PROCESSING, COMPLETED, ERROR
    result TEXT, -- JSON result or Error message
    request_payload JSONB, -- The original request
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_jobs_status ON jobs(status);
