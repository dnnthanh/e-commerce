ALTER TABLE inventory_ledger ADD CONSTRAINT uq_inventory_ledger_reference_reason UNIQUE(reference_key, reason);
