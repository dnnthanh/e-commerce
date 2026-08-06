ALTER TABLE review ADD COLUMN edit_version INT NOT NULL DEFAULT 0;

CREATE TABLE review_helpful (
  review_id BIGINT NOT NULL REFERENCES review(id),
  user_id VARCHAR(64) NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY(review_id, user_id)
);

CREATE TABLE review_report (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  review_id BIGINT NOT NULL REFERENCES review(id),
  reporter_user_id VARCHAR(64) NOT NULL,
  reason VARCHAR(255) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL,
  UNIQUE(review_id, reporter_user_id)
);
