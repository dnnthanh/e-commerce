-- Practice on a separate table. MySQL partitioning has key/unique-key restrictions; do not retrofit it blindly onto the production-like schema.
CREATE TABLE review_history_lab (
  id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  created_at DATE NOT NULL,
  rating INT NOT NULL,
  PRIMARY KEY(id, created_at)
)
PARTITION BY RANGE COLUMNS(created_at)(
  PARTITION p2026h1 VALUES LESS THAN ('2026-07-01'),
  PARTITION p2026h2 VALUES LESS THAN ('2027-01-01'),
  PARTITION pmax VALUES LESS THAN (MAXVALUE)
);
