CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Define ENUM for sender
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'sender') THEN
        CREATE TYPE sender AS ENUM ('USER', 'CHAT_BOT');
    END IF;
END$$;

-- Create Chat table
CREATE TABLE IF NOT EXISTS chats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid()
);

-- Create Message table
CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message TEXT NOT NULL,
    time BIGINT NOT NULL,
    sender sender NOT NULL,
    chat_id UUID REFERENCES chats(id) ON DELETE CASCADE
);
