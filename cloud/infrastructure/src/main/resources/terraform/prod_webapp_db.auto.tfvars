webapp_db_config = {
  port = 5432
  allocated_storage = 20
  instance_class = "db.t2.micro"
  skip_final_snapshot = false
  db_name = "prod"
}
