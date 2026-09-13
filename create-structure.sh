#!/bin/bash

set -e

BASE_DIR="$HOME/microservices_medical"

# ============================================================
# FUNCTION: CREATE STANDARD MICROSERVICE STRUCTURE
# ============================================================

create_standard_service() {

    SERVICE_NAME=$1
    PACKAGE_NAME=$2

    SERVICE_DIR="$BASE_DIR/$SERVICE_NAME"

    echo ""
    echo "=========================================="
    echo "Creating structure for: $SERVICE_NAME"
    echo "=========================================="

    if [ ! -d "$SERVICE_DIR" ]; then
        echo "ERROR: $SERVICE_DIR does not exist."
        echo "Generate the Spring Boot project first."
        return
    fi

    JAVA_DIR="$SERVICE_DIR/src/main/java"

    # Convert package name to path
    PACKAGE_PATH=$(echo "$PACKAGE_NAME" | tr '.' '/')

    BASE_PACKAGE="$JAVA_DIR/$PACKAGE_PATH"

    # --------------------------------------------------------
    # CREATE MAIN PACKAGES
    # --------------------------------------------------------

    mkdir -p \
        "$BASE_PACKAGE/config" \
        "$BASE_PACKAGE/controller" \
        "$BASE_PACKAGE/dto/request" \
        "$BASE_PACKAGE/dto/response" \
        "$BASE_PACKAGE/entity" \
        "$BASE_PACKAGE/model" \
        "$BASE_PACKAGE/repository" \
        "$BASE_PACKAGE/service" \
        "$BASE_PACKAGE/service/impl" \
        "$BASE_PACKAGE/exception" \
        "$BASE_PACKAGE/mapper" \
        "$BASE_PACKAGE/client" \
        "$BASE_PACKAGE/security" \
        "$BASE_PACKAGE/util" \
        "$BASE_PACKAGE/constants" \
        "$BASE_PACKAGE/event"

    # --------------------------------------------------------
    # CREATE RESOURCE FOLDERS
    # --------------------------------------------------------

    mkdir -p \
        "$SERVICE_DIR/src/main/resources/db/migration"

    # --------------------------------------------------------
    # CREATE TEST STRUCTURE
    # --------------------------------------------------------

    TEST_DIR="$SERVICE_DIR/src/test/java/$PACKAGE_PATH"

    mkdir -p \
        "$TEST_DIR/controller" \
        "$TEST_DIR/service" \
        "$TEST_DIR/repository"

    echo "SUCCESS: Structure created for $SERVICE_NAME"
}


# ============================================================
# SPECIAL STRUCTURE: API GATEWAY
# ============================================================

create_gateway_structure() {

    SERVICE_NAME="api-gateway"
    PACKAGE_NAME="com.arvind.gateway"

    SERVICE_DIR="$BASE_DIR/$SERVICE_NAME"
    PACKAGE_PATH=$(echo "$PACKAGE_NAME" | tr '.' '/')
    BASE_PACKAGE="$SERVICE_DIR/src/main/java/$PACKAGE_PATH"

    echo ""
    echo "Creating API Gateway structure..."

    mkdir -p \
        "$BASE_PACKAGE/config" \
        "$BASE_PACKAGE/filter" \
        "$BASE_PACKAGE/security" \
        "$BASE_PACKAGE/exception" \
        "$BASE_PACKAGE/util"

    echo "SUCCESS: API Gateway structure created."
}


# ============================================================
# SPECIAL STRUCTURE: SERVICE REGISTRY
# ============================================================

create_registry_structure() {

    SERVICE_NAME="service-registry"
    PACKAGE_NAME="com.arvind.registry"

    SERVICE_DIR="$BASE_DIR/$SERVICE_NAME"
    PACKAGE_PATH=$(echo "$PACKAGE_NAME" | tr '.' '/')
    BASE_PACKAGE="$SERVICE_DIR/src/main/java/$PACKAGE_PATH"

    echo ""
    echo "Creating Service Registry structure..."

    mkdir -p \
        "$BASE_PACKAGE/config" \
        "$BASE_PACKAGE/exception"

    echo "SUCCESS: Service Registry structure created."
}


# ============================================================
# SPECIAL STRUCTURE: CONFIG SERVER
# ============================================================

create_config_structure() {

    SERVICE_NAME="config-server"

    PACKAGE_NAME="com.arvind.config"

    SERVICE_DIR="$BASE_DIR/$SERVICE_NAME"
    PACKAGE_PATH=$(echo "$PACKAGE_NAME" | tr '.' '/')
    BASE_PACKAGE="$SERVICE_DIR/src/main/java/$PACKAGE_PATH"

    echo ""
    echo "Creating Config Server structure..."

    mkdir -p \
        "$BASE_PACKAGE/config" \
        "$BASE_PACKAGE/exception"

    echo "SUCCESS: Config Server structure created."
}


# ============================================================
# CREATE SPECIAL SERVICES
# ============================================================

create_registry_structure

create_config_structure

create_gateway_structure


# ============================================================
# CREATE STANDARD MICROSERVICES
# ============================================================

create_standard_service "auth-service" "com.arvind.auth"

create_standard_service "user-service" "com.arvind.user"

create_standard_service "medicine-service" "com.arvind.medicine"

create_standard_service "inventory-service" "com.arvind.inventory"

create_standard_service "cart-service" "com.arvind.cart"

create_standard_service "order-service" "com.arvind.order"

create_standard_service "payment-service" "com.arvind.payment"

create_standard_service "prescription-service" "com.arvind.prescription"

create_standard_service "notification-service" "com.arvind.notification"

create_standard_service "delivery-service" "com.arvind.delivery"


# ============================================================
# CREATE COMMON FOLDERS
# ============================================================

mkdir -p "$BASE_DIR/common/common-dto"
mkdir -p "$BASE_DIR/common/common-exception"
mkdir -p "$BASE_DIR/common/common-security"


# ============================================================
# FINISHED
# ============================================================

echo ""
echo "================================================"
echo "ALL FOLDER STRUCTURES CREATED SUCCESSFULLY!"
echo "================================================"
