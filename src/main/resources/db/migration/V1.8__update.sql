ALTER TABLE project.talent
    ADD COLUMN IF NOT EXISTS application_count bigint DEFAULT 0,
    ADD COLUMN IF NOT EXISTS submission_count bigint DEFAULT 0;

WITH app_counts AS (SELECT talent_id, COUNT(*) as app_count
                    FROM project.application
                    GROUP BY talent_id),
     sub_counts AS (SELECT a.talent_id, COUNT(*) as sub_count
                    FROM project.application a
                             JOIN project.submission s ON s.application_id = a.id
                    GROUP BY a.talent_id)
UPDATE project.talent t
SET application_count = COALESCE(ac.app_count, 0),
    submission_count  = COALESCE(sc.sub_count, 0)
FROM app_counts ac
LEFT JOIN sub_counts sc
ON ac.talent_id = sc.talent_id
WHERE t.id = ac.talent_id;