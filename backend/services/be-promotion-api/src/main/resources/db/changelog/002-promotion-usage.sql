CREATE TABLE promotion_usage_counter (
    promotion_id VARCHAR(128) PRIMARY KEY,
    reserved_count BIGINT NOT NULL DEFAULT 0,
    confirmed_count BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE promotion_customer_usage (
    promotion_id VARCHAR(128) NOT NULL,
    customer_id VARCHAR(128) NOT NULL,
    reserved_count BIGINT NOT NULL DEFAULT 0,
    confirmed_count BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (promotion_id, customer_id)
);

CREATE TABLE promotion_usage_reservation (
    reservation_key VARCHAR(256) PRIMARY KEY,
    promotion_id VARCHAR(128) NOT NULL,
    customer_id VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
