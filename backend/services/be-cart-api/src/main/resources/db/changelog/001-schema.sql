-- Schema only. Performance indexes and partitions intentionally live in database-labs.
CREATE TABLE cart (id BIGINT PRIMARY KEY AUTO_INCREMENT, cart_key VARCHAR(128) NOT NULL UNIQUE, user_id VARCHAR(64) NULL, status VARCHAR(32) NOT NULL, updated_at DATETIME NOT NULL);
CREATE TABLE cart_item (id BIGINT PRIMARY KEY AUTO_INCREMENT, cart_id BIGINT NOT NULL, seller_id BIGINT NOT NULL, sku_id BIGINT NOT NULL, quantity INT NOT NULL, price_snapshot DECIMAL(19,2) NULL, updated_at DATETIME NOT NULL, UNIQUE(cart_id, sku_id), CONSTRAINT fk_cart_item_cart FOREIGN KEY(cart_id) REFERENCES cart(id));
