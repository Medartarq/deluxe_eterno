CREATE TABLE IF NOT EXISTS demo_environment (id TINYINT PRIMARY KEY, app_key VARCHAR(80) NOT NULL, dataset_version VARCHAR(30), initialized_at TIMESTAMP);
INSERT INTO demo_environment (id,app_key) SELECT 1,'deluxe-eterno-demo' WHERE NOT EXISTS (SELECT 1 FROM demo_environment WHERE id=1);
