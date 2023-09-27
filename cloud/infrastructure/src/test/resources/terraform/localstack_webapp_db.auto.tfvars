webapp_db_config = {
  port = 4510
  allocated_storage = 1
  instance_class = "db.t2.micro"
  skip_final_snapshot = true
  db_name = "test_db"
}

webapp_db_credentials = {
  username = "test_user"
  password = "test_pw"
}