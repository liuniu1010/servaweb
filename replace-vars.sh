#!/bin/bash

# Script to replace variables in format <xxx> with actual values from environment file
# Usage: ./replace-vars.sh [env-file]

set -e

ENV_FILE="${1:-env.config}"

if [ ! -f "$ENV_FILE" ]; then
    echo "Error: Environment file '$ENV_FILE' not found"
    echo "Usage: $0 [env-file]"
    exit 1
fi

echo "Using environment file: $ENV_FILE"
echo "Starting variable replacement..."

# List of files to process
FILES=(
    "initservamysql57.sql"
    "initservasqlserver2022.sql"
    "nginx/conf/nginx.conf"
    "runimage_local.sh"
    "runnginx.sh"
    "runservamysql57.sh"
    "src/main/webapp/robots.txt"
    "src/main/webapp/sitemap.xml"
)

# Read the environment file and perform replacements
while IFS='=' read -r key value || [ -n "$key" ]; do
    # Skip empty lines and comments
    [[ -z "$key" || "$key" =~ ^[[:space:]]*# ]] && continue

    # Trim whitespace
    key=$(echo "$key" | xargs)
    value=$(echo "$value" | xargs)

    # Skip if key or value is empty
    [ -z "$key" ] || [ -z "$value" ] && continue

    echo "Replacing <$key> with $value"

    # Replace in specified files only
    for file in "${FILES[@]}"; do
        if [ -f "$file" ]; then
            sed -i "s|<$key>|$value|g" "$file"
        else
            echo "Warning: File not found: $file"
        fi
    done

done < "$ENV_FILE"

echo "Variable replacement completed successfully!"
echo ""
echo "Modified files:"
git status --short
