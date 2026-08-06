CREATE TABLE media_processing_asset (
  media_id BIGINT PRIMARY KEY REFERENCES media_asset(id),
  owner_id VARCHAR(64) NOT NULL,
  checksum VARCHAR(128) NOT NULL UNIQUE,
  status VARCHAR(32) NOT NULL,
  reference_count INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

CREATE TABLE media_processing_variant (
  media_id BIGINT NOT NULL REFERENCES media_processing_asset(media_id),
  variant_key VARCHAR(256) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  PRIMARY KEY(media_id, variant_key)
);
