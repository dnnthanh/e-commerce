ALTER TABLE return_request ADD COLUMN rejection_reason VARCHAR(500) NULL;
ALTER TABLE return_request ADD COLUMN dispute_reason VARCHAR(500) NULL;

ALTER TABLE return_line ADD COLUMN already_returned_quantity INT NOT NULL DEFAULT 0;
ALTER TABLE return_line ADD COLUMN refundable_unit_amount NUMERIC(19,2) NULL;
ALTER TABLE return_line ADD COLUMN inventory_disposition VARCHAR(32) NOT NULL DEFAULT 'NONE';

UPDATE return_line
SET refundable_unit_amount = CASE
    WHEN quantity > 0 THEN refundable_amount / quantity
    ELSE 0
END
WHERE refundable_unit_amount IS NULL;

UPDATE return_line SET accepted_quantity = 0 WHERE accepted_quantity IS NULL;
ALTER TABLE return_line ALTER COLUMN refundable_unit_amount SET NOT NULL;
ALTER TABLE return_line ALTER COLUMN accepted_quantity SET NOT NULL;
