CREATE TABLE IF NOT EXISTS reservations (
  id BIGINT NOT NULL AUTO_INCREMENT,
  reservation_number VARCHAR(30) NOT NULL,
  reservation_date DATE NOT NULL,
  reservation_time TIME NOT NULL,
  participants INT NOT NULL,
  customer_name VARCHAR(100) NOT NULL,
  email VARCHAR(254) NOT NULL,
  age_group VARCHAR(20) NOT NULL,
  visit_type VARCHAR(10) NOT NULL,
  unit_price DECIMAL(10,0) NOT NULL,
  total_price DECIMAL(10,0) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_reservations_number (reservation_number),
  KEY idx_reservations_schedule (reservation_date, reservation_time),
  CONSTRAINT chk_participants CHECK (participants >= 1),
  CONSTRAINT chk_visit_type CHECK (visit_type IN ('FIRST','REPEAT')),
  CONSTRAINT chk_status CHECK (status IN ('RECEIVED','CONFIRMED','COMPLETED','CANCELED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE reservations CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
