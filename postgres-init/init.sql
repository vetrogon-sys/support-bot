CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";


-- Create Chat table
CREATE TABLE IF NOT EXISTS chats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid()
);

-- Create Message table
CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message TEXT NOT NULL,
    time BIGINT NOT NULL,
    message_type VARCHAR(30) NOT NULL,
    chat_id UUID REFERENCES chats(id) ON DELETE CASCADE
);
