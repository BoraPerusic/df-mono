CREATE TABLE models (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    provider VARCHAR(50) NOT NULL, -- e.g. 'openai', 'ollama'
    model_type VARCHAR(50) NOT NULL,
    tags TEXT[],
    config TEXT, -- JSON blob (stored as text to avoid JDBC casting issues)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255),
    roles TEXT[] DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
