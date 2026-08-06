CREATE INDEX idx_review_product_status_created
ON review(product_id, status, created_at DESC);
