CREATE INDEX IF NOT EXISTS ix_lab_line_attr_path ON lab_order_line USING gin(attributes jsonb_path_ops);
CREATE INDEX IF NOT EXISTS ix_lab_promo_validity ON lab_promotion USING gist(validity);
CREATE INDEX IF NOT EXISTS ix_lab_promo_scope ON lab_promotion USING gin(scope jsonb_path_ops);
CREATE INDEX IF NOT EXISTS ix_lab_promo_seller ON lab_promotion(seller_id);
-- jsonb_path_ops is smaller/faster for @> but does not support every operator that jsonb_ops supports. Test actual operator set before choosing it.
