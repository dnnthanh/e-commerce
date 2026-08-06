CREATE TABLE seller_staff (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seller_id BIGINT NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    permissions_csv TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at DATETIME NOT NULL,
    UNIQUE(seller_id, user_id),
    CONSTRAINT fk_seller_staff_seller FOREIGN KEY (seller_id) REFERENCES seller(id)
);
