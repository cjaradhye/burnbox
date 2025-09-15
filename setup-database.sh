#!/bin/bash

# PostgreSQL Database Setup Script for Burnbox
echo "Setting up PostgreSQL database for Burnbox..."

# Database configuration
DB_NAME="burnbox"
DB_USER="burnbox_user"
DB_PASSWORD="burnbox_password"

echo "Detected macOS..."
psql postgres <<EOF
CREATE DATABASE $DB_NAME;
CREATE USER $DB_USER WITH ENCRYPTED PASSWORD '$DB_PASSWORD';
GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;
ALTER USER $DB_USER CREATEDB;
EOF

echo "Database setup completed!"
echo "Database: $DB_NAME"
echo "User: $DB_USER"
echo "Password: $DB_PASSWORD"

echo ""
echo "Next steps:"
if [[ "$OSTYPE" == "darwin"* ]]; then
  echo "1. Make sure PostgreSQL is running: brew services start postgresql"
else
  echo "1. Make sure PostgreSQL is running: sudo systemctl start postgresql"
fi
echo "2. Update application.yml with your PostgreSQL connection details"
echo "3. Run the Spring Boot application to execute Flyway migrations"