ALTER TABLE return_line ADD COLUMN ordered_quantity INT NULL;
ALTER TABLE return_line ADD COLUMN delivered_at TIMESTAMP NULL;
ALTER TABLE return_line ADD COLUMN accepted_quantity INT NULL;

UPDATE return_line SET ordered_quantity=quantity WHERE ordered_quantity IS NULL;
UPDATE return_line rl
SET delivered_at=rr.created_at
FROM return_request rr
WHERE rr.id=rl.return_id AND rl.delivered_at IS NULL;

ALTER TABLE return_line ALTER COLUMN ordered_quantity SET NOT NULL;
ALTER TABLE return_line ALTER COLUMN delivered_at SET NOT NULL;
