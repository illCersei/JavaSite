ALTER TABLE ledger_entries
    ADD CONSTRAINT uc_ledger_entries_wallet_reference UNIQUE (wallet_id, reference_type, reference_id);
