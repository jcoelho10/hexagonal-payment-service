provider "aws" {
  region = "us-east-1"
}

# 1. Banco de Dados RDS PostgreSQL
resource "aws_db_instance" "postgres" {
  allocated_storage    = 20
  engine               = "postgres"
  engine_version       = "16.1"
  instance_class       = "db.t4g.micro"
  db_name              = "payment_db"
  username             = "postgres"
  password             = var.db_password
  skip_final_snapshot  = true
  publicly_accessible = false
}

# 2. Servidor de Aplicação AWS App Runner
resource "aws_apprunner_service" "payment_service" {
  service_name = "payment-service"

  source_configuration {
    image_repository {
      image_identifier      = "${var.ecr_image_url}:latest"
      image_repository_type = "ECR"
      image_configuration {
        port = "8081"
        runtime_environment_variables = {
          "SPRING_PROFILES_ACTIVE" = "prod"
          "SPRING_DATASOURCE_URL"  = "jdbc:postgresql://${aws_db_instance.postgres.endpoint}/payment_db"
          "SPRING_DATASOURCE_USERNAME" = "postgres"
          "SPRING_DATASOURCE_PASSWORD" = var.db_password
        }
      }
    }
    auto_deployments_enabled = true
  }
}