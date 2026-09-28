provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "rg" {
  name     = "rg-payment-prod"
  location = "East US"
}

# 1. PostgreSQL Flexible Server
resource "azurerm_postgresql_flexible_server" "postgres" {
  name                   = "psql-payment-prod"
  resource_group_name    = azurerm_resource_group.rg.name
  location               = azurerm_resource_group.rg.location
  version                = "16"
  administrator_login    = "psqladmin"
  administrator_password = var.db_password
  sku_name               = "B_Standard_B1ms"
  storage_mb             = 32768
}

# 2. Azure Container App (Execução do Spring Boot)
resource "azurerm_container_app_environment" "env" {
  name                = "cae-payment"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
}

resource "azurerm_container_app" "app" {
  name                         = "payment-service"
  container_app_environment_id = azurerm_container_app_environment.env.id
  resource_group_name          = azurerm_resource_group.rg.name
  revision_mode                = "Single"

  template {
    container {
      name   = "payment-service"
      image  = "mcr.microsoft.com/azuredocs/aci-helloworld:latest" # Substituir pela sua imagem
      cpu    = 0.5
      memory = "1.0Gi"

      env {
        name  = "SPRING_DATASOURCE_URL"
        value = "jdbc:postgresql://${azurerm_postgresql_flexible_server.postgres.fqdn}:5432/payment_db"
      }
      env {
        name  = "SPRING_DATASOURCE_USERNAME"
        value = "psqladmin"
      }
      env {
        name  = "SPRING_DATASOURCE_PASSWORD"
        value = var.db_password
      }
    }
  }
}