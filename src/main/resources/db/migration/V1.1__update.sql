-- Add producer column to project table
ALTER TABLE project 
    ADD COLUMN producer VARCHAR(100);