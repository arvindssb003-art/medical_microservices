#!/bin/bash

set -e

BASE_DIR="$HOME/microservices_medical"
CHATBOT_DIR="$BASE_DIR/chatbot-service"

echo "=============================================="
echo "Creating Chatbot Service Structure"
echo "=============================================="

# CREATE DIRECTORIES

mkdir -p "$CHATBOT_DIR/app/api/routes"
mkdir -p "$CHATBOT_DIR/app/clients"
mkdir -p "$CHATBOT_DIR/app/config"
mkdir -p "$CHATBOT_DIR/app/core"
mkdir -p "$CHATBOT_DIR/app/dependencies"
mkdir -p "$CHATBOT_DIR/app/models"
mkdir -p "$CHATBOT_DIR/app/schemas"
mkdir -p "$CHATBOT_DIR/app/services"
mkdir -p "$CHATBOT_DIR/app/repositories"
mkdir -p "$CHATBOT_DIR/app/utils"
mkdir -p "$CHATBOT_DIR/app/exceptions"
mkdir -p "$CHATBOT_DIR/app/prompts"

mkdir -p "$CHATBOT_DIR/tests/api"
mkdir -p "$CHATBOT_DIR/tests/clients"
mkdir -p "$CHATBOT_DIR/tests/services"
mkdir -p "$CHATBOT_DIR/tests/repositories"

mkdir -p "$CHATBOT_DIR/data"
mkdir -p "$CHATBOT_DIR/logs"

# PYTHON PACKAGE FILES

touch "$CHATBOT_DIR/app/__init__.py"

touch "$CHATBOT_DIR/app/api/__init__.py"
touch "$CHATBOT_DIR/app/api/routes/__init__.py"

touch "$CHATBOT_DIR/app/clients/__init__.py"
touch "$CHATBOT_DIR/app/config/__init__.py"
touch "$CHATBOT_DIR/app/core/__init__.py"
touch "$CHATBOT_DIR/app/dependencies/__init__.py"
touch "$CHATBOT_DIR/app/models/__init__.py"
touch "$CHATBOT_DIR/app/schemas/__init__.py"
touch "$CHATBOT_DIR/app/services/__init__.py"
touch "$CHATBOT_DIR/app/repositories/__init__.py"
touch "$CHATBOT_DIR/app/utils/__init__.py"
touch "$CHATBOT_DIR/app/exceptions/__init__.py"

touch "$CHATBOT_DIR/tests/__init__.py"

# MAIN FILE

touch "$CHATBOT_DIR/app/main.py"

# API ROUTES

touch "$CHATBOT_DIR/app/api/routes/chat.py"
touch "$CHATBOT_DIR/app/api/routes/health.py"

# JAVA MICROSERVICE CLIENTS

touch "$CHATBOT_DIR/app/clients/medicine_client.py"
touch "$CHATBOT_DIR/app/clients/order_client.py"
touch "$CHATBOT_DIR/app/clients/user_client.py"
touch "$CHATBOT_DIR/app/clients/inventory_client.py"
touch "$CHATBOT_DIR/app/clients/auth_client.py"

# CONFIGURATION

touch "$CHATBOT_DIR/app/config/settings.py"
touch "$CHATBOT_DIR/app/config/database.py"

# CORE

touch "$CHATBOT_DIR/app/core/constants.py"
touch "$CHATBOT_DIR/app/core/security.py"

# DEPENDENCIES

touch "$CHATBOT_DIR/app/dependencies/auth.py"

# MODELS

touch "$CHATBOT_DIR/app/models/conversation.py"
touch "$CHATBOT_DIR/app/models/message.py"

# SCHEMAS

touch "$CHATBOT_DIR/app/schemas/chat.py"
touch "$CHATBOT_DIR/app/schemas/conversation.py"
touch "$CHATBOT_DIR/app/schemas/message.py"

# SERVICES

touch "$CHATBOT_DIR/app/services/chat_service.py"
touch "$CHATBOT_DIR/app/services/intent_service.py"
touch "$CHATBOT_DIR/app/services/conversation_service.py"
touch "$CHATBOT_DIR/app/services/llm_service.py"
touch "$CHATBOT_DIR/app/services/rag_service.py"

# REPOSITORIES

touch "$CHATBOT_DIR/app/repositories/conversation_repository.py"
touch "$CHATBOT_DIR/app/repositories/message_repository.py"

# UTILITIES

touch "$CHATBOT_DIR/app/utils/logger.py"
touch "$CHATBOT_DIR/app/utils/helpers.py"

# EXCEPTIONS

touch "$CHATBOT_DIR/app/exceptions/custom_exceptions.py"
touch "$CHATBOT_DIR/app/exceptions/exception_handlers.py"

# PROMPTS

touch "$CHATBOT_DIR/app/prompts/system_prompt.txt"
touch "$CHATBOT_DIR/app/prompts/medical_shop_prompt.txt"

# TESTS

touch "$CHATBOT_DIR/tests/api/test_chat.py"
touch "$CHATBOT_DIR/tests/api/test_health.py"

touch "$CHATBOT_DIR/tests/clients/test_medicine_client.py"
touch "$CHATBOT_DIR/tests/clients/test_order_client.py"

touch "$CHATBOT_DIR/tests/services/test_chat_service.py"
touch "$CHATBOT_DIR/tests/services/test_intent_service.py"

touch "$CHATBOT_DIR/tests/repositories/test_conversation_repository.py"

# ROOT FILES

touch "$CHATBOT_DIR/requirements.txt"
touch "$CHATBOT_DIR/.env"
touch "$CHATBOT_DIR/.env.example"
touch "$CHATBOT_DIR/.gitignore"
touch "$CHATBOT_DIR/Dockerfile"
touch "$CHATBOT_DIR/README.md"

echo ""
echo "=============================================="
echo "CHATBOT SERVICE CREATED SUCCESSFULLY!"
echo "=============================================="
