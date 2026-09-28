provider "google" {
  project = "your-gcp-project-id"
  region  = "us-central1"
}

# 1. Cloud SQL Instance (PostgreSQL 16)
resource "google_sql_database_instance" "postgres" {
  name             = "pg-payment-instance"
  database_version = "POSTGRES_16"
  region           = "us-central1"

  settings {
    tier = "db-f1-micro"
  }
}

# 2. Cloud Run Service
resource "google_cloud_run_v2_service" "payment_service" {
  name     = "payment-service"
  location = "us-central1"

  template {
    containers {
      image = "gcr.io/your-gcp-project-id/payment-service:latest"

      resources {
        limits = {
          cpu    = "1000m"
          memory = "512Mi"
        }
      }

      env {
        name  = "SPRING_DATASOURCE_URL"
        value = "jdbc:postgresql:///${google_sql_database_instance.postgres.public_ip_address}:5432/payment_db"
      }
      env {
        name  = "SPRING_DATASOURCE_USERNAME"
        value = "postgres"
      }
      env {
        name  = "SPRING_DATASOURCE_PASSWORD"
        value = var.db_password
      }
    }
  }
}