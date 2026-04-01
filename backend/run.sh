#!/bin/bash
set +H
set -e

# Load .env from project root
ENV_FILE="$(dirname "$0")/../.env"
if [ -f "$ENV_FILE" ]; then
    while IFS='=' read -r key value; do
        [[ "$key" =~ ^#.*$ || -z "$key" ]] && continue
        export "$key=$value"
    done < "$ENV_FILE"
fi

cd "$(dirname "$0")"
./mvnw spring-boot:run
