-- Add reward images column (run manually if not using JPA ddl-auto)
ALTER TABLE rewards ADD COLUMN images VARCHAR(2000);
