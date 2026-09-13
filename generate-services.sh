#!/bin/bash

set -e

# ============================================================
# ONLINE MEDICAL SHOP - MICROSERVICES GENERATOR
# ============================================================

BASE_URL="https://start.spring.io/starter.zip"

# ---------------- PROJECT CONFIGURATION ----------------

GROUP_ID="com.arvind"

JAVA_VERSION="21"

SPRING_BOOT_VERSION="4.0.8"

# Choose ONE database:
# mysql OR postgresql
DATABASE="postgresql"
# ============================================================
# DEPENDENCY GROUPS
# ============================================================

# Basic dependencies for REST microservices
COMMON_WEB="web,validation,lombok,actuator,devtools"

# Database and JPA
DATABASE_DEPS="data-jpa,$DATABASE"

# Service discovery
EUREKA_CLIENT="cloud-eureka"

# Inter-service communication
OPENFEIGN="cloud-feign"

# Security
SECURITY="security"

# Production / infrastructure dependencies
# Add these if supported by your Spring Initializr version
# flyway
# prometheus
# resilience4j

# ============================================================
# FUNCTION TO GENERATE SERVICE
# ============================================================

generate_service() {

    SERVICE_NAME=$1
    PACKAGE_NAME=$2
    DEPENDENCIES=$3

    echo ""
    echo "================================================"
    echo "Creating: $SERVICE_NAME"
    echo "Java: $JAVA_VERSION"
    echo "Spring Boot: $SPRING_BOOT_VERSION"
    echo "Dependencies: $DEPENDENCIES"
    echo "================================================"

    # Check if directory already exists
    if [ -d "$SERVICE_NAME" ] && [ "$(ls -A "$SERVICE_NAME")" ]; then
        echo "WARNING: $SERVICE_NAME already exists."
        echo "Skipping..."
        return
    fi

    mkdir -p "$SERVICE_NAME"

    curl -s -G "$BASE_URL" \
        --data-urlencode "type=maven-project" \
        --data-urlencode "language=java" \
        --data-urlencode "bootVersion=$SPRING_BOOT_VERSION" \
        --data-urlencode "javaVersion=$JAVA_VERSION" \
        --data-urlencode "groupId=$GROUP_ID" \
        --data-urlencode "artifactId=$SERVICE_NAME" \
        --data-urlencode "name=$SERVICE_NAME" \
        --data-urlencode "packageName=$PACKAGE_NAME" \
        --data-urlencode "dependencies=$DEPENDENCIES" \
        -o "$SERVICE_NAME/project.zip"

    # Verify file exists
    if [ ! -s "$SERVICE_NAME/project.zip" ]; then
        echo "ERROR: Failed to download $SERVICE_NAME"
        exit 1
    fi

    # Extract project
    unzip -q "$SERVICE_NAME/project.zip" \
        -d "$SERVICE_NAME"

    # Remove zip
    rm "$SERVICE_NAME/project.zip"

    echo "SUCCESS: $SERVICE_NAME created!"
}


# ============================================================
# 1. SERVICE REGISTRY
# ============================================================

# Dependencies from your XML:
# Eureka Server
# Actuator
# DevTools

generate_service \
    "service-registry" \
    "com.arvind.registry" \
    "cloud-eureka-server,actuator,devtools"


# ============================================================
# 2. CONFIG SERVER
# ============================================================

generate_service \
    "config-server" \
    "com.arvind.config" \
    "cloud-config-server,actuator,devtools"


# ============================================================
# 3. API GATEWAY
# ============================================================

# Dependencies:
# Gateway
# Load Balancer
# Eureka Client
# Actuator

generate_service \
    "api-gateway" \
    "com.arvind.gateway" \
    "cloud-gateway,cloud-loadbalancer,cloud-eureka,actuator,devtools"


# ============================================================
# 4. AUTH SERVICE
# ============================================================

# Dependencies:
# Web
# JPA
# Security
# Validation
# Lombok
# Database
# Eureka Client
# OpenFeign
# Actuator
# DevTools

generate_service \
    "auth-service" \
    "com.arvind.auth" \
    "web,data-jpa,security,validation,lombok,$DATABASE,cloud-eureka,cloud-feign,actuator,devtools"


# ============================================================
# 5. USER SERVICE
# ============================================================

generate_service \
    "user-service" \
    "com.arvind.user" \
    "web,data-jpa,validation,lombok,$DATABASE,cloud-eureka,cloud-feign,actuator,devtools"


# ============================================================
# 6. MEDICINE SERVICE
# ============================================================

generate_service \
    "medicine-service" \
    "com.arvind.medicine" \
    "web,data-jpa,validation,lombok,$DATABASE,cloud-eureka,cloud-feign,actuator,devtools"


# ============================================================
# 7. INVENTORY SERVICE
# ============================================================

generate_service \
    "inventory-service" \
    "com.arvind.inventory" \
    "web,data-jpa,validation,lombok,$DATABASE,cloud-eureka,cloud-feign,actuator,devtools"


# ============================================================
# 8. CART SERVICE
# ============================================================

generate_service \
    "cart-service" \
    "com.arvind.cart" \
    "web,data-jpa,validation,lombok,$DATABASE,cloud-eureka,cloud-feign,actuator,devtools"


# ============================================================
# 9. ORDER SERVICE
# ============================================================

generate_service \
    "order-service" \
    "com.arvind.order" \
    "web,data-jpa,validation,lombok,$DATABASE,cloud-eureka,cloud-feign,actuator,devtools"


# ============================================================
# 10. PAYMENT SERVICE
# ============================================================

generate_service \
    "payment-service" \
    "com.arvind.payment" \
    "web,data-jpa,security,validation,lombok,$DATABASE,cloud-eureka,cloud-feign,actuator,devtools"


# ============================================================
# 11. PRESCRIPTION SERVICE
# ============================================================

generate_service \
    "prescription-service" \
    "com.arvind.prescription" \
    "web,data-jpa,security,validation,lombok,$DATABASE,cloud-eureka,actuator,devtools"


# ============================================================
# 12. NOTIFICATION SERVICE
# ============================================================

generate_service \
    "notification-service" \
    "com.arvind.notification" \
    "web,validation,lombok,cloud-eureka,cloud-feign,actuator,devtools"


# ============================================================
# 13. DELIVERY SERVICE
# ============================================================

generate_service \
    "delivery-service" \
    "com.arvind.delivery" \
    "web,data-jpa,validation,lombok,$DATABASE,cloud-eureka,cloud-feign,actuator,devtools"


# ============================================================
# PROJECT GENERATION COMPLETE
# ============================================================

echo ""
echo "================================================"
echo "ALL MICROSERVICES CREATED SUCCESSFULLY"
echo "================================================"

echo "Group ID:          $GROUP_ID"
echo "Java Version:      $JAVA_VERSION"
echo "Spring Boot:       $SPRING_BOOT_VERSION"
echo "Database:          $DATABASE"

echo ""
echo "Generated services:"

echo "1.  service-registry"
echo "2.  config-server"
echo "3.  api-gateway"
echo "4.  auth-service"
echo "5.  user-service"
echo "6.  medicine-service"
echo "7.  inventory-service"
echo "8.  cart-service"
echo "9.  order-service"
echo "10. payment-service"
echo "11. prescription-service"
echo "12. notification-service"
echo "13. delivery-service"

echo "================================================"
