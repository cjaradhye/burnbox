# Database initialization script for PostgreSQL
# This script will run automatically when the PostgreSQL container starts

-- Create the burnbox database (already created by POSTGRES_DB env var)
-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE burnbox TO burnbox;

-- Create tables if they don't exist (Flyway will handle migrations)
-- This is just for initial setup

\echo 'PostgreSQL initialization completed successfully!'