output rds_endpoint {
  value = startswith(aws_db_instance.webapp_db.endpoint, "localhost") ? "localstack:${aws_db_instance.webapp_db.port}" : aws_db_instance.webapp_db.endpoint
}

output arn {
  value = aws_db_instance.webapp_db.arn
}