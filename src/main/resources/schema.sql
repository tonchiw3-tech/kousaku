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
  CONSTRAINT chk_status CHECK (status IN ('RECEIVED','CONFIRMED','READY','COMPLETED','CANCELED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE reservations CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reservation_status_corrections (
  id BIGINT NOT NULL AUTO_INCREMENT,
  reservation_id BIGINT NOT NULL,
  before_status VARCHAR(20) NOT NULL,
  after_status VARCHAR(20) NOT NULL,
  changed_at DATETIME NOT NULL,
  operator_username VARCHAR(100) NOT NULL,
  reason VARCHAR(1000) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_corrections_reservation (reservation_id, changed_at),
  CONSTRAINT fk_corrections_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inquiries (
  id BIGINT NOT NULL AUTO_INCREMENT,
  inquiry_number VARCHAR(30) NOT NULL,
  customer_name VARCHAR(100) NOT NULL,
  email VARCHAR(254) NOT NULL,
  category VARCHAR(20) NOT NULL,
  message TEXT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'NEW',
  reply_message TEXT NULL,
  replied_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_inquiries_number (inquiry_number),
  KEY idx_inquiries_status_created (status, created_at),
  CONSTRAINT chk_inquiries_category CHECK (category IN ('PRICE','AVAILABILITY','SOLO','DURATION','ACCESS','RAINY','FRAGRANCE','OTHER')),
  CONSTRAINT chk_inquiries_status CHECK (status IN ('NEW','IN_PROGRESS','REPLIED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
