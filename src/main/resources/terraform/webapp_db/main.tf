resource "aws_secretsmanager_secret" "webapp_db_credentials_secret" {
  recovery_window_in_days = 0 // Overriding the default recovery window of 30 days, so that it can be immediately deleted
}

resource "aws_secretsmanager_secret_version" "webapp_db_credentials_secret_value" {
  secret_id     = aws_secretsmanager_secret.webapp_db_credentials_secret.id
  secret_string = jsonencode(var.webapp_db_credentials)
}

data "aws_secretsmanager_secret_version" "webapp_db_credentials_secret_data" {
  depends_on = [aws_secretsmanager_secret_version.webapp_db_credentials_secret_value]
  secret_id     = aws_secretsmanager_secret.webapp_db_credentials_secret.id
}

resource "aws_db_instance" "webapp_db" {
  allocated_storage    = var.webapp_db_config.allocated_storage
  storage_type         = "gp2"
  engine               = "postgres"
  engine_version       = "15"
  instance_class       = var.webapp_db_config.instance_class
  skip_final_snapshot  = var.webapp_db_config.skip_final_snapshot
  username             = jsondecode(data.aws_secretsmanager_secret_version.webapp_db_credentials_secret_data.secret_string)["username"]
  password             = jsondecode(data.aws_secretsmanager_secret_version.webapp_db_credentials_secret_data.secret_string)["password"]
  db_name              = var.webapp_db_config.db_name
  port                 = var.webapp_db_config.port
}