-- Database: review_db (MySQL 8.4)
-- Run each candidate separately and compare EXPLAIN ANALYZE.

-- Product review timeline. MySQL secondary indexes already carry the PK, so no INCLUDE syntax.
CREATE INDEX idx_review_product_status_created
ON review(product_id,status,created_at DESC,rating);

-- Verified purchase lookup used by review eligibility.
CREATE INDEX idx_verified_purchase_user_product_delivered
ON verified_purchase(user_id,product_id,delivered_at DESC,order_line_id);

-- Moderation/report lookup.
CREATE INDEX idx_review_report_status_created
ON review_report(status,created_at DESC,review_id);

-- Helpful leaderboard join.
CREATE INDEX idx_review_helpful_review_created
ON review_helpful(review_id,created_at DESC,user_id);
